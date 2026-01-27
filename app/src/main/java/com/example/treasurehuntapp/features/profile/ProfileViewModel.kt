package com.example.treasurehuntapp.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.UsersRepository
import com.example.treasurehuntapp.data.source.remote.dto.user.UpdateUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    fun loadMe() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                val me = usersRepository.getCurrentUser()
                state = state.copy(
                    loading = false,
                    user = me,
                    email = me.email,
                    username = me.username,
                    firstName = me.firstName,
                    lastName = me.lastName
                )
            } catch (e: Exception) {
                state = state.copy(
                    loading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun onEmail(v: String) { state = state.copy(email = v) }
    fun onUsername(v: String) { state = state.copy(username = v) }
    fun onFirstName(v: String) { state = state.copy(firstName = v) }
    fun onLastName(v: String) { state = state.copy(lastName = v) }

    fun startEdit(field: ProfileField) { state = state.copy(currentlyEditing = field) }
    fun cancelEdit() { state = state.copy(currentlyEditing = null) }

    fun save() {
        val user = state.user ?: return

        viewModelScope.launch {
            state = state.copy(saving = true, error = null)

            try {
                val dto = UpdateUserDto(
                    email = state.email.takeIf { it.trim() != user.email }?.trim(),
                    username = state.username.takeIf { it.trim() != user.username }?.trim(),
                    firstName = state.firstName.takeIf { it.trim() != user.firstName }?.trim(),
                    lastName = state.lastName.takeIf { it.trim() != user.lastName }?.trim(),
                )

                val updated = usersRepository.updateUser(user.id, dto)

                state = state.copy(
                    saving = false,
                    user = updated,
                    email = updated.email ?: state.email,
                    username = updated.username ?: state.username,
                    firstName = updated.firstName ?: state.firstName,
                    lastName = updated.lastName ?: state.lastName,
                    currentlyEditing = null
                )
            } catch (e: Exception) {
                state = state.copy(
                    saving = false,
                    error = e.message ?: "Failed to save profile"
                )
            }
        }
    }

    fun deleteAccount() {
        val user = state.user ?: return
        viewModelScope.launch {
            state = state.copy(deleting = true, deleteError = null)
            try {
                usersRepository.deleteUser(user.id)
                state = state.copy(deleting = false, deleted = true)
            } catch (e: Exception) {
                state = state.copy(deleting = false, deleteError = e.message ?: "Failed to delete account")
            }
        }
    }
}
