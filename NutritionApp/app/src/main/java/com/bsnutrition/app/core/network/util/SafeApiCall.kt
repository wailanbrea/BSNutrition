package com.bsnutrition.app.core.network.util

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.ApiErrorResponse
import com.bsnutrition.app.core.model.ApiException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    json: Json,
    apiCall: suspend () -> Response<T>
): Result<T> {
    return withContext(dispatcher) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    Result.Error(
                        exception = NullPointerException("Response body is null"),
                        message = "Response body is null"
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val apiException = parseApiError(errorBody, response.code(), json)
                Result.Error(
                    exception = apiException,
                    message = apiException.message,
                    code = apiException.code
                )
            }
        } catch (e: IOException) {
            Result.Error(
                exception = e,
                message = "No se pudo conectar con el servidor. Verifica tu conexión a internet.",
                code = "NETWORK_UNAVAILABLE"
            )
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "Ocurrió un error inesperado.",
                code = "UNEXPECTED_ERROR"
            )
        }
    }
}

fun parseApiError(errorBody: String?, statusCode: Int, json: Json): ApiException {
    if (errorBody.isNullOrBlank()) {
        return ApiException(
            code = "HTTP_$statusCode",
            message = "HTTP error $statusCode",
            httpStatusCode = statusCode
        )
    }

    return try {
        val errorResponse = json.decodeFromString<ApiErrorResponse>(errorBody)
        ApiException(
            code = errorResponse.error.code,
            message = errorResponse.error.message,
            fields = errorResponse.error.fields,
            httpStatusCode = statusCode
        )
    } catch (e: Exception) {
        ApiException(
            code = "HTTP_$statusCode",
            message = "HTTP error $statusCode: $errorBody",
            httpStatusCode = statusCode
        )
    }
}
