package com.example.treasurehuntapp.features.active_hunt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ActiveHuntViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ActiveHuntState())
    val state = _state.asStateFlow()
}
