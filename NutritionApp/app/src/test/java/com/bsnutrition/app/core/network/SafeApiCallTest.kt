package com.bsnutrition.app.core.network

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.common.onError
import com.bsnutrition.app.core.common.onSuccess
import com.bsnutrition.app.core.model.ApiException
import com.bsnutrition.app.core.network.util.parseApiError
import com.bsnutrition.app.core.network.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SafeApiCallTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun parseApiError_withValidationJson_parsesFieldsCorrectly() {
        val errorJson = """
            {
                "error": {
                    "code": "VALIDATION_ERROR",
                    "message": "The given data was invalid.",
                    "fields": {
                        "email": ["The email field is required."],
                        "password": ["The password must be at least 8 characters."]
                    }
                }
            }
        """.trimIndent()

        val apiException = parseApiError(errorJson, 422, json)

        assertEquals("VALIDATION_ERROR", apiException.code)
        assertEquals("The given data was invalid.", apiException.message)
        assertEquals(422, apiException.httpStatusCode)
        assertEquals(2, apiException.fields.size)
        assertEquals("The email field is required.", apiException.fields["email"]?.first())
    }

    @Test
    fun parseApiError_withUnauthenticatedJson_parsesCorrectly() {
        val errorJson = """
            {
                "error": {
                    "code": "UNAUTHENTICATED",
                    "message": "Unauthenticated.",
                    "fields": {}
                }
            }
        """.trimIndent()

        val apiException = parseApiError(errorJson, 401, json)

        assertEquals("UNAUTHENTICATED", apiException.code)
        assertEquals("Unauthenticated.", apiException.message)
        assertEquals(401, apiException.httpStatusCode)
    }

    @Test
    fun safeApiCall_withSuccessfulResponse_returnsResultSuccess() = runTest {
        val result = safeApiCall(Dispatchers.Unconfined, json) {
            Response.success("Success Data")
        }

        assertTrue(result is Result.Success)
        result.onSuccess { data ->
            assertEquals("Success Data", data)
        }
    }

    @Test
    fun safeApiCall_withErrorResponse_returnsResultErrorWithApiException() = runTest {
        val errorBody = """
            {
                "error": {
                    "code": "NOT_FOUND",
                    "message": "Resource not found.",
                    "fields": {}
                }
            }
        """.trimIndent().toResponseBody(null)

        val result = safeApiCall(Dispatchers.Unconfined, json) {
            Response.error<String>(404, errorBody)
        }

        assertTrue(result is Result.Error)
        result.onError { exception, message, code ->
            assertTrue(exception is ApiException)
            assertEquals("NOT_FOUND", code)
            assertEquals("Resource not found.", message)
        }
    }
}
