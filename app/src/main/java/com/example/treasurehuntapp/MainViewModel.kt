package com.example.treasurehuntapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.AuthRepository
import com.example.treasurehuntapp.data.source.remote.auth.TokenStorage
import com.example.treasurehuntapp.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()
    val accessToken = tokenStorage.accessToken

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            val hasValidSession = authRepository.validateSession()

            _state.value = MainState(
                resolving = false,
                startDestination = if (hasValidSession) {
                    Destinations.Home.route
                } else {
                    Destinations.Login.route
                }
            )
        }
    }
}
