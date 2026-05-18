package com.example.treasurehuntapp.features.create_location

data class EditLocationState(
    val locationId: String = "",
    val name: String = "",
    val question: String = "",
    val correctAnswer: String = "",
    val hint: String = "",
    val imageUri: String = "",
    val originalImageUrl: String? = null,
    val imageLabel: String = "Add location image (tap to select)",
    val lat: String = "",
    val lng: String = "",
    val isLoading: Boolean = false,
    val saving: Boolean = false,
    val allowSaveWithoutImage: Boolean = false,
    val error: String? = null,
)
