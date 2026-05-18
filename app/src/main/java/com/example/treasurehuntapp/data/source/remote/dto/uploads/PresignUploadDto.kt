package com.example.treasurehuntapp.data.source.remote.dto.uploads

data class PresignUploadDto(
    val kind: String,
    val filename: String,
    val contentType: String,
    val size: Long
)
