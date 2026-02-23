package org.radarbase.config.model

import kotlinx.serialization.Serializable

@Serializable
data class StudyDefinition(
    val projectId: String,
    val studyId: String,
    val version: String,
    val metadata: Metadata,
    val population: Population,
    val recruitment: Recruitment? = null,
    val domain: Domain,
    val eligibility: Eligibility,
    val design: Design,
    val technology: Technology,
    val contact: Contact,
    val team: Team? = null,
    val sources: Sources,
    val questionnaires: List<Questionnaire>,
)

@Serializable
data class Metadata(
    val name: String,
    val description: String,
    val healthIssues: List<String> = emptyList(),
    val lastUpdated: String? = null,
    val maintainer: String? = null,
    val status: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
)

@Serializable
data class Population(
    val ageRange: AgeRange,
    val maxParticipants: Int,
    val location: String,
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
data class Outcomes(
    val primary: List<String>,
    val secondary: List<String>,
)

@Serializable
data class Technology(
    val devices: List<Device>,
    val notes: String? = null,
)

@Serializable
data class Device(
    val id: String,
    val dataTypes: List<String>,
)

@Serializable
data class Contact(
    val email: String,
    val resources: List<String>,
)

@Serializable
data class Sources(
    val enrolment: SourceRef,
    val protocol: SourceRef,
)

@Serializable
data class SourceRef(
    val href: String,
    val description: String,
)

@Serializable
data class Questionnaire(
    val id: String,
    val name: String,
    val href: String,
    val numberOfQuestions: Int? = null,
    val estimatedMinutes: Int? = null,
    val createdDate: String? = null,
)

@Serializable
data class CreateQuestionnaireRequest(
    val questionnaire: Questionnaire,
    val body: String? = null,
)

@Serializable
data class UpdateQuestionnaireRequest(
    val questionnaire: Questionnaire,
    val body: String? = null,
)

@Serializable
data class Recruitment(
    val type: String,
)

@Serializable
data class Team(
    val researchers: List<Researcher>,
)

@Serializable
data class Researcher(
    val name: String,
    val email: String,
    val role: String,
)
