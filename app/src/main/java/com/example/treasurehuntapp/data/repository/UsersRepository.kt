package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.UsersApi
import com.example.treasurehuntapp.data.source.remote.dto.user.UpdateUserDto
import com.example.treasurehuntapp.data.source.remote.dto.user.UserDto
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val api: UsersApi
) {

    suspend fun getAllUsers(): List<UserDto> {
        return api.getAll().data
    }

    suspend fun getCurrentUser(): UserDto {
        return api.getMe().data
    }

    suspend fun getUserById(id: String): UserDto {
        return api.getById(id).data
    }

    suspend fun updateUser(id: String, dto: UpdateUserDto): UserDto {
        return api.update(id, dto).data
    }

    suspend fun deleteUser(id: String) {
        api.delete(id)
    }
}
