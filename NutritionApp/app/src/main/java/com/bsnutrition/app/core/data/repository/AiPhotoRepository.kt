package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.AiPhotoAnalysis
import java.io.File

interface AiPhotoRepository {

    suspend fun analyzeFoodPhoto(
        imageFile: File,
        locale: String = "DO",
        mealType: String = "lunch"
    ): Result<AiPhotoAnalysis>

    suspend fun getAnalysis(id: Long): Result<AiPhotoAnalysis>

    suspend fun confirmAnalysis(
        analysisId: Long,
        date: String,
        mealType: String,
        items: List<ConfirmedPhotoItem>
    ): Result<Int>
}

data class ConfirmedPhotoItem(
    val name: String,
    val foodId: Long?,
    val portionId: Long? = null,
    val quantity: Double = 1.0,
    val weightGrams: Double,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double
)
