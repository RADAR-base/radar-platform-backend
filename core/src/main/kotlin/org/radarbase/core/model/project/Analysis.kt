package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

/** Analysis configuration including features and visualizations. */
@Serializable
data class Analysis(
    val features: List<String>,
    val visualizations: List<Visualization>,
)

@Serializable
data class Visualization(
    val title: String,
    val type: String,
    val sources: List<String>,
)
