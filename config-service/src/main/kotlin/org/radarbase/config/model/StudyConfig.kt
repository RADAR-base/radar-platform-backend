package org.radarbase.config.model

import kotlinx.serialization.Serializable

/**
 * Generic study configuration model.
 *
 * We keep the structure flexible by storing the raw JSON as JsonElement so
 * different studies can evolve independently.
 */
@Serializable
data class StudyConfig(
    val projectName: String,
    val source: String,
    val config: StudyDefinition,
)
