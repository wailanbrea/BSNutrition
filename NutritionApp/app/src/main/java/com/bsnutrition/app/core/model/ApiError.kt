package com.bsnutrition.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val error: ApiErrorDetail
)

@Serializable
data class ApiErrorDetail(
    val code: String,
    val message: String,
    val fields: Map<String, List<String>> = emptyMap()
)

class ApiException(
    val code: String,
    override val message: String,
    val fields: Map<String, List<String>> = emptyMap(),
    val httpStatusCode: Int = 0
) : Exception(message)
