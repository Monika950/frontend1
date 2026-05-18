package com.example.treasurehuntapp.features.profile

import com.example.treasurehuntapp.data.source.remote.dto.user.UserDto

enum class ProfileField {
    Email, Username, FirstName, LastName
}

data class ProfileState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,

    val user: UserDto? = null,

    val email: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",

    val currentlyEditing: ProfileField? = null,
    val huntsCompletedCount: Int = 0,
    val huntsCreatedCount: Int = 0,
    val deleting: Boolean = false,
    val deleteError: String? = null,
    val deleted: Boolean = false,
    val loggingOut: Boolean = false,
    val logoutError: String? = null,
    val loggedOut: Boolean = false,
    val changingPassword: Boolean = false,
    val changePasswordCurrent: String = "",
    val changePasswordNew: String = "",
    val changePasswordConfirm: String = "",
    val changePasswordError: String? = null,
    val changePasswordSuccess: String? = null,
)
