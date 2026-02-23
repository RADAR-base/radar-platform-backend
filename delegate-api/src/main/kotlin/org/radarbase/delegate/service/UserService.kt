package org.radarbase.delegate.service

import org.radarbase.delegate.model.User

interface UserService {
    suspend fun getUsers(
        projectId: String,
        authToken: String?,
    ): List<User>

    suspend fun getUser(
        projectId: String,
        userId: String,
        authToken: String?,
    ): User?

    suspend fun createUser(
        projectId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>

    suspend fun updateUser(
        projectId: String,
        userId: String,
        body: String,
        authToken: String?,
    ): Pair<Int, String>
}
