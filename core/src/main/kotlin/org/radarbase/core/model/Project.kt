package org.radarbase.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val projectId: String,
    val name: String,
    val description: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val location: String,
    val population: Population,
    val domain: Domain,
    val eligibility: Eligibility,
    val design: Design,
    val technology: Technology,
    val analysis: Analysis,
    val contact: Contact,
)

@Serializable
data class Population(
    val ageRange: AgeRange,
    val maxParticipants: Int,
)

@Serializable
data class AgeRange(
    val min: Int,
    val max: Int,
)

@Serializable
data class Domain(
    val area: String,
    val keywords: List<String>,
)

@Serializable
data class Eligibility(
    val inclusion: List<String>,
    val exclusion: List<String>,
)

@Serializable
data class Design(
    val phases: List<String>,
    val currentPhase: String? = null,
    val measurements: List<Measurement>,
    val intervention: Intervention,
    val outcomes: Outcomes,
)

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

@Serializable
data class Technology(
    val devices: List<String>,
    val dataTypes: List<String>,
    val frequency: String,
    val notes: String?,
)

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

@Serializable
data class Contact(
    val email: String,
    val resources: List<String>,
)

