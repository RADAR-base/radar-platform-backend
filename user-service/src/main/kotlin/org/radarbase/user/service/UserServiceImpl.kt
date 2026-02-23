package org.radarbase.user.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.radarbase.user.client.KratosClient
import org.radarbase.user.client.RadarClient
import org.radarbase.user.model.CreateParticipantRequest
import org.radarbase.user.model.KratosCreateIdentityRequest
import org.radarbase.user.model.KratosProject
import org.radarbase.user.model.KratosSubjectWebhookDTO
import org.radarbase.user.model.KratosTraits
import org.radarbase.user.model.KratosUser
import org.radarbase.user.model.ManagementPortalProxyResponse
import org.radarbase.user.model.RadarUser
import org.radarbase.user.model.TriggerRecoveryEmailRequest
import org.radarbase.user.model.UpdateUserRequest
import org.radarbase.user.model.UserDTO
import org.slf4j.LoggerFactory

@Singleton
class UserServiceImpl
    @Inject
    constructor(
        private val kratosClient: KratosClient,
        private val radarClient: RadarClient,
    ) : UserService {
        private val logger = LoggerFactory.getLogger(UserServiceImpl::class.java)

        // Cache mapping of Radar user IDs to Kratos user IDs
        private val userAssociationCache = mutableMapOf<String, String>()
        private val participantAssociationCache = mutableMapOf<String, String>()
        private val userMutex = Mutex()
        private val participantMutex = Mutex()

        override suspend fun getUsers(
            projectId: String,
            authToken: String,
        ): List<CombinedUser> {
            val radarUsers = radarClient.getUsers(projectId)
            val kratosUsers = kratosClient.getIdentities().filter { it.schemaId != PARTICIPANT_SCHEMA_ID }

            return combineUserData(radarUsers, kratosUsers, projectId)
        }

        override suspend fun getUser(
            projectId: String,
            userId: String,
            authToken: String,
        ): CombinedUser? {
            // userId is the Kratos identity ID; resolve Radar ID (login) for Radar API calls
            val kratosUser = kratosClient.getIdentity(userId)
            val radarId = resolveRadarUserId(projectId, userId)
            val radarUser = radarId?.let { radarClient.getUser(projectId, it) }

            return if (radarUser != null || kratosUser != null) {
                createCombinedUser(radarUser, kratosUser, projectId)
            } else {
                null
            }
        }

        override suspend fun getUserByEmail(
            projectId: String,
            email: String,
            authToken: String,
        ): CombinedUser? {
            val kratosUser = kratosClient.getIdentityByEmail(email)
            return createCombinedUser(null, kratosUser, projectId)
        }

        override suspend fun getParticipants(
            projectId: String,
            authToken: String,
        ): List<CombinedUser> {
            val radarParticipants = radarClient.getParticipants(projectId)
            val kratosUsers = kratosClient.getIdentities().filter { it.schemaId == PARTICIPANT_SCHEMA_ID }

            val combinedUsers = combineUserData(radarParticipants, kratosUsers, projectId)

            return combinedUsers.filterNotNull()
        }

        override suspend fun getParticipant(
            projectId: String,
            participantId: String,
            authToken: String,
        ): CombinedUser? {
            // participantId is the Kratos identity ID; resolve Radar ID (project userId) for Radar API calls
            val kratosUser = kratosClient.getIdentity(participantId)?.takeIf { it.schemaId == PARTICIPANT_SCHEMA_ID }
            val radarId = resolveRadarParticipantId(projectId, participantId, kratosUser)
            val radarParticipant = radarId?.let { radarClient.getParticipant(projectId, it) }

            return if (radarParticipant != null || kratosUser != null) {
                createCombinedUser(radarParticipant, kratosUser, projectId)
            } else {
                null
            }
        }

        /**
         * Get a participant by their email address
         * @param projectId The Radar project ID
         * @param email The participant's email address
         * @param authToken The authorization token for Radar API
         * @return Participant information or null if not found
         */
        override suspend fun getParticipantByEmail(
            projectId: String,
            email: String,
            authToken: String,
        ): CombinedUser? {
            val kratosUser =
                kratosClient.getIdentityByEmail(email)?.takeIf { it.schemaId == PARTICIPANT_SCHEMA_ID }
                    ?: return null

            val radarId = resolveRadarParticipantId(projectId, kratosUser.id, kratosUser)
            val radarParticipant = radarId?.let { radarClient.getParticipant(projectId, it) }

            return createCombinedUser(radarParticipant, kratosUser, projectId)
        }

        override suspend fun updateUser(
            projectId: String,
            userId: String,
            updateRequest: UpdateUserRequest,
            authToken: String,
        ): CombinedUser? {
            logger.debug("Updating user $userId for project $projectId (userId = Kratos identity ID)")
            // userId is the Kratos identity ID; resolve Radar ID (login) for Radar API calls
            val currentKratosUser = kratosClient.getIdentity(userId)
            val radarId = resolveRadarUserId(projectId, userId)
            val currentRadarUser = radarId?.let { radarClient.getUser(projectId, it) }

            if (currentRadarUser == null && currentKratosUser == null) {
                logger.warn("User $userId not found in either Radar or Kratos")
                return null
            }

            var updatedRadarUser: RadarUser? = null
            var updatedKratosUser: KratosUser? = null
            var radarUpdateSuccess = true
            var kratosUpdateSuccess = true

            // Update Radar if we have a Radar user (currentRadarUser implies radarId was set)
            @Suppress("SENSELESS_COMPARISON")
            if (currentRadarUser != null && radarId != null) {
                updatedRadarUser = applyRadarUpdates(currentRadarUser, updateRequest)
                val result = radarClient.updateUser(projectId, radarId, updatedRadarUser)
                if (result == null) {
                    logger.error("Failed to update user $userId (radarId=$radarId) in Radar")
                    radarUpdateSuccess = false
                } else {
                    updatedRadarUser = result
                }
            }

            // Update Kratos if we have a Kratos user
            if (currentKratosUser != null) {
                updatedKratosUser = applyKratosUpdates(currentKratosUser, updateRequest)
                val result = kratosClient.updateIdentity(userId, updatedKratosUser)
                if (result == null) {
                    logger.error("Failed to update user $userId in Kratos")
                    kratosUpdateSuccess = false
                } else {
                    updatedKratosUser = result
                }
            }

            // If both updates failed, return null
            if (!radarUpdateSuccess && !kratosUpdateSuccess) {
                logger.error("Failed to update user $userId in both Radar and Kratos")
                return null
            }

            // Return the updated combined user
            return createCombinedUser(
                updatedRadarUser ?: currentRadarUser,
                updatedKratosUser ?: currentKratosUser,
                projectId,
            )
        }

        override suspend fun updateParticipant(
            projectId: String,
            participantId: String,
            updateRequest: UpdateUserRequest,
            authToken: String,
        ): CombinedUser? {
            logger.debug("Updating participant $participantId for project $projectId (participantId = Kratos identity ID)")
            // participantId is the Kratos identity ID; resolve Radar ID (project userId) for Radar API calls
            val currentKratosUser =
                kratosClient
                    .getIdentity(participantId)
                    ?.takeIf { it.schemaId == PARTICIPANT_SCHEMA_ID }
            val radarId = resolveRadarParticipantId(projectId, participantId, currentKratosUser)
            val currentRadarParticipant = radarId?.let { radarClient.getParticipant(projectId, it) }

            if (currentRadarParticipant == null && currentKratosUser == null) {
                logger.warn("Participant $participantId not found in either Radar or Kratos")
                return null
            }

            var updatedRadarParticipant: RadarUser? = null
            var updatedKratosUser: KratosUser? = null
            var radarUpdateSuccess = true
            var kratosUpdateSuccess = true

            // Update Radar if we have a Radar participant (currentRadarParticipant implies radarId was set)
            @Suppress("SENSELESS_COMPARISON")
            if (currentRadarParticipant != null && radarId != null) {
                updatedRadarParticipant = applyRadarUpdates(currentRadarParticipant, updateRequest)
                val result = radarClient.updateParticipant(projectId, radarId, updatedRadarParticipant)
                if (result == null) {
                    logger.error("Failed to update participant $participantId (radarId=$radarId) in Radar")
                    radarUpdateSuccess = false
                } else {
                    updatedRadarParticipant = result
                }
            }

            // Update Kratos if we have a Kratos user
            if (currentKratosUser != null) {
                updatedKratosUser = applyKratosUpdates(currentKratosUser, updateRequest)
                val result = kratosClient.updateIdentity(participantId, updatedKratosUser)
                if (result == null) {
                    logger.error("Failed to update participant $participantId in Kratos")
                    kratosUpdateSuccess = false
                } else {
                    updatedKratosUser = result
                }
            }

            // If both updates failed, return null
            if (!radarUpdateSuccess && !kratosUpdateSuccess) {
                logger.error("Failed to update participant $participantId in both Radar and Kratos")
                return null
            }

            // Return the updated combined user
            return createCombinedUser(
                updatedRadarParticipant ?: currentRadarParticipant,
                updatedKratosUser ?: currentKratosUser,
                projectId,
            )
        }

        override suspend fun createParticipant(
            projectId: String,
            request: CreateParticipantRequest,
            authToken: String,
        ): CombinedUser? {
            logger.debug("Creating participant in Kratos and syncing to Management Portal (projectId={})", projectId)

            val createIdentityRequest =
                KratosCreateIdentityRequest(
                    schemaId = PARTICIPANT_SCHEMA_ID,
                    traits =
                        KratosTraits(
                            email = request.email,
                            projects =
                                listOf(
                                    KratosProject(
                                        id = projectId,
                                        name = null,
                                        userId = request.projectUserId,
                                    ),
                                ),
                        ),
                    state = request.state,
                )

            val createdIdentity = kratosClient.createIdentity(createIdentityRequest) ?: return null

            val webhookOk =
                radarClient.callKratosSubjectsWebhook(
                    KratosSubjectWebhookDTO(
                        identity =
                            KratosSubjectWebhookDTO.Identity(
                                id = createdIdentity.id,
                                schemaId = createdIdentity.schemaId,
                                traits =
                                    KratosSubjectWebhookDTO.Traits(
                                        email = request.email,
                                        projects =
                                            listOf(
                                                KratosSubjectWebhookDTO.ProjectTrait(
                                                    id = projectId,
                                                    userId = request.projectUserId,
                                                ),
                                            ),
                                    ),
                            ),
                    ),
                )
            if (!webhookOk) {
                logger.warn(
                    "Participant {} created in Kratos but Management Portal webhook call failed",
                    createdIdentity.id,
                )
            }

            // Best-effort: the webhook may process asynchronously, so Radar might not have it immediately.
            val radarParticipant =
                radarClient.getParticipant(
                    projectId,
                    createdIdentity.traits.projects
                        .first()
                        .userId,
                )
            return createCombinedUser(radarParticipant, createdIdentity, projectId)
        }

        override suspend fun createUser(
            projectId: String,
            request: UserDTO,
            authToken: String,
        ): ManagementPortalProxyResponse {
            val json = Json { encodeDefaults = true }.encodeToString(request)
            return radarClient.createUserRaw(projectId, json)
        }

        override suspend fun triggerRecoveryEmail(
            request: TriggerRecoveryEmailRequest,
            authToken: String,
        ): Boolean {
            val method = request.method?.trim().takeUnless { it.isNullOrEmpty() } ?: "link"
            return kratosClient.triggerRecoveryEmail(request.email, method)
        }

        /**
         * Apply updates to a RadarUser object based on the update request
         */
        private fun applyRadarUpdates(
            currentUser: RadarUser,
            updateRequest: UpdateUserRequest,
        ): RadarUser =
            currentUser.copy(
                status = updateRequest.status ?: currentUser.status,
                group = updateRequest.studyGroup ?: currentUser.group,
                attributes = updateRequest.attributes ?: currentUser.attributes,
                dateOfBirth = updateRequest.dateOfBirth ?: currentUser.dateOfBirth,
                personName = updateRequest.personName ?: currentUser.personName,
            )

        /**
         * Apply updates to a KratosUser object based on the update request
         */
        private fun applyKratosUpdates(
            currentUser: KratosUser,
            updateRequest: UpdateUserRequest,
        ): KratosUser {
            val updatedTraits =
                updateRequest.traits ?: run {
                    val currentTraits = currentUser.traits
                    if (updateRequest.email != null) {
                        currentTraits.copy(email = updateRequest.email)
                    } else {
                        currentTraits
                    }
                }

            val updatedState = updateRequest.state ?: currentUser.state
            val updatedMetadataPublic = updateRequest.metadataPublic ?: currentUser.metadataPublic

            return currentUser.copy(
                state = updatedState,
                traits = updatedTraits,
                metadataPublic = updatedMetadataPublic,
            )
        }

        private fun combineUserData(
            radarUsers: List<RadarUser>,
            kratosUsers: List<KratosUser>,
            projectId: String,
        ): List<CombinedUser> {
            val kratosById = kratosUsers.associateBy { it.id }
            val kratosByMpLogin = kratosUsers.associateBy { it.metadataPublic?.mpLogin }
            val kratosByEmail = kratosUsers.associateBy { it.traits.email }

            return radarUsers.map { radarUser ->
                val kratosUser =
                    radarUser.identity
                        ?.let { kratosById[it] }
                        ?: kratosByMpLogin[radarUser.login]
                        ?: kratosByEmail[radarUser.login]
                        ?: kratosByEmail[radarUser.externalId]

                createCombinedUser(radarUser, kratosUser, projectId)
            }
        }

        private fun createCombinedUser(
            radarUser: RadarUser?,
            kratosUser: KratosUser?,
            projectId: String,
        ): CombinedUser {
            val id = kratosUser?.id ?: radarUser?.login ?: throw IllegalArgumentException("kratosUser must be non-null")
            val email = kratosUser?.traits?.email ?: ""

            return CombinedUser(
                id = id,
                projectId = projectId,
                email = email,
                radarMpId = radarUser?.login,
                radarInfo = radarUser,
                kratosInfo = kratosUser,
            )
        }

        /**
         * Resolve Radar user ID (login) from Kratos identity ID.
         * Lists project users and finds the one whose identity equals the Kratos ID.
         */
        private suspend fun resolveRadarUserId(
            projectId: String,
            kratosUserId: String,
        ): String? {
            val users = radarClient.getUsers(projectId)
            return users.find { it.identity == kratosUserId }?.login
        }

        /**
         * Resolve Radar participant ID (project userId / subject login) from Kratos identity.
         * Uses the project's userId from traits.projects for the given projectId.
         */
        @Suppress("UNUSED_PARAMETER")
        private fun resolveRadarParticipantId(
            projectId: String,
            kratosParticipantId: String,
            kratosUser: KratosUser?,
        ): String? =
            kratosUser
                ?.traits
                ?.projects
                ?.find { it.id == projectId }
                ?.userId

        companion object {
            private val PARTICIPANT_SCHEMA_ID = "subject"
            private val RESEARCHER_SCHEMA_ID = "researcher"
            private val ADMIN_SCHEMA_ID = "admin"
        }
    }
