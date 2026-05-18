package com.example.treasurehuntapp.features.create_hunt

data class CreateHuntState(
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val startIso: String = "",
    val endIso: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
