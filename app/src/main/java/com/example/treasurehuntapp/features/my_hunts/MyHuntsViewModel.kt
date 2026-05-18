package com.example.treasurehuntapp.features.my_hunts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.repository.UploadsRepository
import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto
import com.example.treasurehuntapp.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class MyHuntsViewModel @Inject constructor(
    private val repo: TreasureHuntRepository,
    private val currentHuntStorage: CurrentHuntStorage,
    private val uploadsRepository: UploadsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MyHuntsData>>(UiState.Loading)
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = UiState.Loading
                val memberships = repo.getMemberships()

                val created = mutableListOf<MyHuntCard>()
                val joined = mutableListOf<MyHuntCard>()

                memberships.forEach { membership ->
                    val hunt = membership.treasureHunt
                    val status = computeStatus(hunt)
                    val dateText = formatDateLine(hunt)

                    val isOwner = membership.role?.equals("owner", ignoreCase = true) == true
                    val card = MyHuntCard(
                        hunt = hunt,
                        imageUrl = runCatching { uploadsRepository.resolveDisplayImageUrl(hunt.image) }.getOrNull(),
                        dateText = dateText,
                        status = status,
                        participants = null,
                        rating = null,
                        ctaText = if (isOwner) "Manage Hunt" else "Continue"
                    )

                    if (isOwner) created.add(card) else joined.add(card)
                }

                _state.value = UiState.Success(
                    MyHuntsData(
                        createdHunts = created,
                        joinedHunts = joined
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load hunts")
            }
        }
    }

    fun openHunt(hunt: TreasureHuntDto, onDone: () -> Unit) {
        viewModelScope.launch {
            currentHuntStorage.saveHunt(hunt.id, hunt.code)
            onDone()
        }
    }

    private fun computeStatus(hunt: TreasureHuntDto): HuntStatus {
        val start = parseIsoMillis(hunt.start)
        val end = parseIsoMillis(hunt.end)
        val now = System.currentTimeMillis()

        return when {
            start != null && now < start -> HuntStatus.Scheduled
            start != null && end != null && now in start..end -> HuntStatus.Active
            else -> HuntStatus.Finished
        }
    }

    private fun formatDateLine(hunt: TreasureHuntDto): String {
        val date = parseIsoMillis(hunt.start)
        if (date == null) return hunt.start
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    private fun parseIsoMillis(iso: String?): Long? {
        val value = iso?.trim()
        if (value.isNullOrEmpty()) return null

        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )

        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(value)
                if (d != null) return d.time
            } catch (_: Exception) {
            }
        }
        return null
    }
}

