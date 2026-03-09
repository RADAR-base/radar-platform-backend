package org.radarbase.core.model.project

import kotlinx.serialization.Serializable

/** The study design: phases, measurements, intervention and outcomes. */
@Serializable
data class Design(
    val phases: List<String>,
    val currentPhase: String? = null,
    val measurements: List<Measurement>,
    val intervention: Intervention,
    val outcomes: Outcomes,
)

/** A single measurement within a study design. */
@Serializable
data class Measurement(
    val type: String,
    val frequency: String,
    val mode: String? = null,
    val description: String? = null,
    val devices: List<String>? = null,
    val dataTypes: List<String>? = null,
)

@Serializable
data class Intervention(
    val description: String,
    val deliveryModes: List<String>,
)

@Serializable
data class Outcomes(
    val primary: List<String>,
    val secondary: List<String>,
)
