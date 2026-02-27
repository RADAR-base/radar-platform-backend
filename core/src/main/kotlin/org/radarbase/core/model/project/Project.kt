package org.radarbase.core.model.project

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
