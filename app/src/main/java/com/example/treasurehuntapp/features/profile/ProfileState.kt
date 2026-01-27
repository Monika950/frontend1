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
    val deleting: Boolean = false,
    val deleteError: String? = null,
    val deleted: Boolean = false,
)
