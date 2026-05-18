package com.example.treasurehuntapp.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.example.treasurehuntapp.data.source.remote.api.UploadsApi
import com.example.treasurehuntapp.data.source.remote.dto.uploads.PresignDownloadDto
import com.example.treasurehuntapp.data.source.remote.dto.uploads.PresignUploadDto
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Named

class UploadsRepository @Inject constructor(
    private val uploadsApi: UploadsApi,
    @Named("noAuthOkHttp") private val noAuthOkHttp: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "UploadsRepository"
        private const val MAX_UPLOAD_BYTES = 2L * 1024L * 1024L
        private val ALLOWED_MIME = setOf("image/jpeg", "image/png", "image/webp")
    }

    private val downloadUrlCache = ConcurrentHashMap<String, String>()

    suspend fun uploadLocationImage(uriString: String): String {
        return uploadImage(uriString = uriString, kind = "location")
    }

    suspend fun uploadHuntImage(uriString: String): String {
        return uploadImage(uriString = uriString, kind = "hunt")
    }

    suspend fun resolveDisplayImageUrl(imageRef: String?): String? {
        val value = imageRef?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("content://") || value.startsWith("file://")) {
            return value
        }

        val extractedS3Key = extractS3KeyIfUnsignedUrl(value)
        if ((value.startsWith("http://") || value.startsWith("https://")) && extractedS3Key == null) {
            return value
        }

        downloadUrlCache[value]?.let { return it }

        return withContext(Dispatchers.IO) {
            val keyCandidates = buildList {
                add(extractedS3Key ?: value)
                if (!value.contains("/")) {
                    add("uploads/hunts/$value")
                    add("uploads/locations/$value")
                }
            }.distinct()

            var lastError: String? = null
            for (key in keyCandidates) {
                try {
                    val responseData = uploadsApi.presignDownload(PresignDownloadDto(key = key)).data
                    val downloadUrl = deepFirstString(
                        responseData,
                        "downloadUrl",
                        "url",
                        "signedUrl",
                        "presignedUrl"
                    ) ?: error("Presign download response missing URL")
                    downloadUrlCache[value] = downloadUrl
                    downloadUrlCache[key] = downloadUrl
                    return@withContext downloadUrl
                } catch (e: HttpException) {
                    lastError = "HTTP ${e.code()}"
                } catch (e: Exception) {
                    lastError = e.message
                }
            }

            Log.w(TAG, "resolveDisplayImageUrl failed for '$value': ${lastError ?: "unknown"}")
            null
        }
    }

    private suspend fun uploadImage(uriString: String, kind: String): String {
        return withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Cannot read selected image")
            val meta = readMeta(context.contentResolver, uri, bytes.size.toLong())
            if (meta.size > MAX_UPLOAD_BYTES) {
                error("Image is too large (${meta.size} bytes). Max size is 2MB.")
            }
            if (meta.contentType !in ALLOWED_MIME) {
                error("Unsupported image type: ${meta.contentType}. Use JPEG, PNG or WEBP.")
            }

            val data = try {
                uploadsApi.presign(
                    PresignUploadDto(
                        kind = kind,
                        filename = meta.fileName,
                        contentType = meta.contentType,
                        size = meta.size
                    )
                ).data
            } catch (e: HttpException) {
                val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
                val apiMessage = parseApiErrorMessage(raw)
                error(apiMessage ?: "Presign failed (${e.code()})")
            }

            val uploadUrl = deepFirstString(
                data,
                "uploadUrl",
                "presignedUrl",
                "url",
                "signedUrl"
            ) ?: error("Presign response missing upload URL")

            val key = deepFirstString(data, "key", "fileKey", "path")
                ?: error("Presign response missing key")
            val requiredHeadersSource = jsonObject(data, "requiredHeaders")
            val uploadContentType = deepFirstString(data, "contentType", "mimeType")
                ?.takeIf { it.isNotBlank() }
                ?: requiredHeadersSource
                    ?.get("Content-Type")
                    ?.takeIf { !it.isJsonNull && it.isJsonPrimitive }
                    ?.asString
                    ?.takeIf { it.isNotBlank() }
                ?: requiredHeadersSource
                    ?.get("content-type")
                    ?.takeIf { !it.isJsonNull && it.isJsonPrimitive }
                    ?.asString
                    ?.takeIf { it.isNotBlank() }
                ?: meta.contentType

            val putRequest = Request.Builder()
                .url(uploadUrl)
                .apply {
                    val headerSource = requiredHeadersSource
                        ?: jsonObject(data, "headers")
                        ?: jsonObject(data, "upload")?.let { jsonObject(it, "headers") }

                    headerSource?.entrySet()?.forEach { entry ->
                        if (!entry.value.isJsonNull) {
                            addHeader(entry.key, entry.value.asString)
                        }
                    }

                    val uploadUri = uploadUrl.toUri()
                    uploadUri.getQueryParameter("x-amz-sdk-checksum-algorithm")
                        ?.takeIf { it.isNotBlank() }
                        ?.let { addHeader("x-amz-sdk-checksum-algorithm", it) }
                    uploadUri.getQueryParameter("x-amz-checksum-crc32")
                        ?.takeIf { it.isNotBlank() }
                        ?.let { addHeader("x-amz-checksum-crc32", it) }
                }
                .put(bytes.toRequestBody(uploadContentType.toMediaTypeOrNull()))
                .build()

            noAuthOkHttp.newCall(putRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val responseBody = runCatching { response.body?.string() }.getOrNull().orEmpty()
                    error(
                        "Image upload failed: HTTP ${response.code} ${
                            if (responseBody.isNotBlank()) "- ${responseBody.take(180)}" else ""
                        }".trim()
                    )
                }
            }

            uploadUrl.substringBefore("?").ifBlank { key }
        }
    }

    private fun firstString(json: JsonObject, vararg keys: String): String? {
        for (key in keys) {
            if (json.has(key) && !json.get(key).isJsonNull) {
                val node = json.get(key)
                if (!node.isJsonPrimitive) continue
                val value = node.asString
                if (value.isNotBlank()) return value
            }
        }
        return null
    }

    private fun deepFirstString(json: JsonObject, vararg keys: String): String? {
        firstString(json, *keys)?.let { return it }
        listOf("upload", "download", "file", "asset", "result", "presign", "s3").forEach { childKey ->
            jsonObject(json, childKey)?.let { child ->
                firstString(child, *keys)?.let { return it }
            }
        }
        return null
    }

    private fun jsonObject(json: JsonObject, key: String): JsonObject? {
        if (!json.has(key) || json.get(key).isJsonNull) return null
        val node = json.get(key)
        return if (node.isJsonObject) node.asJsonObject else null
    }

    private fun readMeta(
        resolver: ContentResolver,
        uri: Uri,
        exactSize: Long
    ): UploadMeta {
        var name: String? = null

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) name = cursor.getString(nameIndex)
            }
        }

        val contentType = normalizeMimeType(
            resolver.getType(uri)
            ?: guessMimeFromName(name)
            ?: "application/octet-stream"
        )

        val finalName = name ?: "upload-${UUID.randomUUID()}.${extensionFromMime(contentType)}"

        return UploadMeta(
            fileName = finalName,
            contentType = contentType,
            size = exactSize
        )
    }

    private fun guessMimeFromName(fileName: String?): String? {
        val ext = fileName?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()
        if (ext.isNullOrBlank()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    }

    private fun extensionFromMime(mime: String): String {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "bin"
    }

    private fun normalizeMimeType(mime: String): String {
        return when (mime.lowercase()) {
            "image/jpg" -> "image/jpeg"
            else -> mime
        }
    }

    private fun extractS3KeyIfUnsignedUrl(value: String): String? {
        if (!value.startsWith("http://") && !value.startsWith("https://")) return null
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return null
        val host = uri.host.orEmpty()
        val looksLikeS3 = host.contains("amazonaws.com", ignoreCase = true)
        if (!looksLikeS3) return null
        val hasSignature = uri.getQueryParameter("X-Amz-Signature") != null ||
            uri.getQueryParameter("x-amz-signature") != null
        if (hasSignature) return null
        return uri.path
            ?.removePrefix("/")
            ?.takeIf { it.isNotBlank() }
    }

    private fun parseApiErrorMessage(raw: String?): String? {
        val body = raw?.trim().orEmpty()
        if (body.isBlank()) return null
        Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        return body.take(180)
    }

    private data class UploadMeta(
        val fileName: String,
        val contentType: String,
        val size: Long
    )
}
