package com.example.treasurehuntapp.features.active_hunt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HuntInfoCard(
    hunt: TreasureHuntDto,
    participantsCount: Int,
    locationsCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InfoRow(
                left = "Start: ${formatHuntDateTime(hunt.start)}",
                right = "End: ${formatHuntDateTime(hunt.end)}"
            )
            InfoRow(
                left = "Locations: $locationsCount",
                right = "Members: $participantsCount"
            )
            hunt.code?.takeIf { it.isNotBlank() }?.let { code ->
                Text(
                    text = "Code: $code",
                    color = Color(0xFFB44BF3),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InfoRow(left: String, right: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = left,
            color = Color(0xFF8C7AA8),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = right,
            color = Color(0xFF8C7AA8),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatHuntDateTime(iso: String?): String {
    val value = iso?.trim()
    if (value.isNullOrEmpty()) return "-"
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        try {
            val input = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed: Date = input.parse(value) ?: continue
            val output = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return output.format(parsed)
        } catch (_: Exception) {
        }
    }
    return value
}
