package com.bsnutrition.app.core

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.common.onError
import com.bsnutrition.app.core.common.onSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun resultSuccess_executesOnSuccess() {
        val result: Result<String> = Result.Success("Hello BSNutrition")
        var receivedValue = ""

        result.onSuccess { data ->
            receivedValue = data
        }.onError { _, _, _ ->
            // should not be called
        }

        assertEquals("Hello BSNutrition", receivedValue)
        assertTrue(result is Result.Success)
    }

    @Test
    fun resultError_executesOnError() {
        val exception = RuntimeException("Network failure")
        val result: Result<String> = Result.Error(exception, "Custom error", "NETWORK_ERROR")
        var errorCalled = false
        var errorCode = ""

        result.onSuccess {
            // should not be called
        }.onError { _, _, code ->
            errorCalled = true
            errorCode = code ?: ""
        }

        assertTrue(errorCalled)
        assertEquals("NETWORK_ERROR", errorCode)
        assertTrue(result is Result.Error)
    }
}
