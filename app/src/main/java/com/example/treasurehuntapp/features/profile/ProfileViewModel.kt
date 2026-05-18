package com.example.treasurehuntapp.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.AuthRepository
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.repository.UsersRepository
import com.example.treasurehuntapp.data.source.remote.dto.user.UpdateUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val treasureHuntRepository: TreasureHuntRepository
) : ViewModel() {

    var state by mutableStateOf(ProfileState())
        private set

    fun loadMe() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                val me = usersRepository.getCurrentUser()
                val memberships = runCatching { treasureHuntRepository.getMemberships() }.getOrDefault(emptyList())
                val now = System.currentTimeMillis()
                val createdCount = memberships.count { it.role.equals("owner", ignoreCase = true) }
                val completedCount = memberships.count { membership ->
                    !membership.role.equals("owner", ignoreCase = true) &&
                        (parseIsoMillis(membership.treasureHunt.end)?.let { it < now } == true)
                }
                state = state.copy(
                    loading = false,
                    user = me,
                    email = me.email,
                    username = me.username,
                    firstName = me.firstName,
                    lastName = me.lastName,
                    huntsCreatedCount = createdCount,
                    huntsCompletedCount = completedCount
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
    fun onCurrentPassword(v: String) { state = state.copy(changePasswordCurrent = v, changePasswordError = null, changePasswordSuccess = null) }
    fun onNewPassword(v: String) { state = state.copy(changePasswordNew = v, changePasswordError = null, changePasswordSuccess = null) }
    fun onConfirmPassword(v: String) { state = state.copy(changePasswordConfirm = v, changePasswordError = null, changePasswordSuccess = null) }

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

    fun logout() {
        viewModelScope.launch {
            state = state.copy(loggingOut = true, logoutError = null)
            try {
                authRepository.logout()
                state = state.copy(loggingOut = false, loggedOut = true)
            } catch (e: Exception) {
                state = state.copy(
                    loggingOut = false,
                    logoutError = e.message ?: "Logout failed"
                )
            }
        }
    }

    fun changePassword() {
        val current = state.changePasswordCurrent
        val newPassword = state.changePasswordNew
        val confirm = state.changePasswordConfirm

        when {
            current.isBlank() || newPassword.isBlank() || confirm.isBlank() -> {
                state = state.copy(changePasswordError = "All password fields are required", changePasswordSuccess = null)
                return
            }
            newPassword != confirm -> {
                state = state.copy(changePasswordError = "Passwords do not match", changePasswordSuccess = null)
                return
            }
        }

        viewModelScope.launch {
            state = state.copy(changingPassword = true, changePasswordError = null, changePasswordSuccess = null)
            try {
                authRepository.changePassword(current, newPassword)
                state = state.copy(
                    changingPassword = false,
                    changePasswordCurrent = "",
                    changePasswordNew = "",
                    changePasswordConfirm = "",
                    changePasswordSuccess = "Password changed successfully"
                )
            } catch (e: Exception) {
                state = state.copy(
                    changingPassword = false,
                    changePasswordError = e.message ?: "Failed to change password"
                )
            }
        }
    }

    private fun parseIsoMillis(iso: String?): Long? {
        val value = iso?.trim().orEmpty()
        if (value.isBlank()) return null
        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (pattern in patterns) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsed = fmt.parse(value)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return null
    }
}
