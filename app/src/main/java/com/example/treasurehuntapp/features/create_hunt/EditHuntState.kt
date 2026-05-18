package com.example.treasurehuntapp.features.create_hunt

data class EditHuntState(
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val startIso: String = "",
    val endIso: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
