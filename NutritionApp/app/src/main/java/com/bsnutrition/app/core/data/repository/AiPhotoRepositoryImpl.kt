package com.bsnutrition.app.core.data.repository

import com.bsnutrition.app.core.common.Result
import com.bsnutrition.app.core.model.AiPhotoAnalysis
import com.bsnutrition.app.core.model.AiPhotoItem
import com.bsnutrition.app.core.model.FoodCandidate
import com.bsnutrition.app.core.network.api.AiPhotoApiService
import com.bsnutrition.app.core.network.dto.ConfirmPhotoAnalysisRequest
import com.bsnutrition.app.core.network.dto.ConfirmPhotoItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiPhotoRepositoryImpl @Inject constructor(
    private val apiService: AiPhotoApiService
) : AiPhotoRepository {

    override suspend fun analyzeFoodPhoto(
        imageFile: File,
        locale: String,
        mealType: String
    ): Result<AiPhotoAnalysis> = withContext(Dispatchers.IO) {
        try {
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
            val localePart = locale.toRequestBody("text/plain".toMediaTypeOrNull())
            val mealTypePart = mealType.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.analyzePhoto(body, localePart, mealTypePart)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.data
                Result.Success(
                    AiPhotoAnalysis(
                        id = dto.id,
                        status = dto.status,
                        dishName = dto.dishName ?: "Plato identificado",
                        summary = dto.summary ?: "",
                        confidenceScore = dto.confidenceScore ?: 0.9,
                        provider = dto.provider ?: "openai",
                        totalCalories = dto.totals?.calories ?: 0,
                        totalProteinG = dto.totals?.proteinG ?: 0.0,
                        totalCarbsG = dto.totals?.carbsG ?: 0.0,
                        totalFatG = dto.totals?.fatG ?: 0.0,
                        items = dto.items.map { item ->
                            AiPhotoItem(
                                id = item.id,
                                foodId = item.foodId,
                                name = item.name,
                                matchedName = item.matchedName,
                                weightGrams = item.estimatedWeightGrams,
                                portionDescription = item.portionDescription ?: "1 porción",
                                preparationMethod = item.preparationMethod,
                                confidence = item.confidence,
                                calories = item.calories,
                                proteinG = item.proteinG,
                                carbsG = item.carbsG,
                                fatG = item.fatG,
                                candidates = item.candidates.map { c ->
                                    FoodCandidate(
                                        foodId = c.foodId,
                                        canonicalName = c.canonicalName,
                                        brandName = c.brandName,
                                        score = c.score,
                                        matchType = c.matchType,
                                        calories100g = c.calories100g,
                                        protein100g = c.protein100g,
                                        carbs100g = c.carbs100g,
                                        fat100g = c.fat100g
                                    )
                                }
                            )
                        }
                    )
                )
            } else {
                Result.Error(
                    exception = Exception("Error al analizar la imagen (${response.code()})"),
                    message = "No se pudo procesar la imagen con IA.",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de conexión con el servicio de IA.")
        }
    }

    override suspend fun getAnalysis(id: Long): Result<AiPhotoAnalysis> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAnalysis(id)
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.data
                Result.Success(
                    AiPhotoAnalysis(
                        id = dto.id,
                        status = dto.status,
                        dishName = dto.dishName ?: "",
                        summary = dto.summary ?: "",
                        confidenceScore = dto.confidenceScore ?: 0.9,
                        provider = dto.provider ?: "openai",
                        totalCalories = dto.totals?.calories ?: 0,
                        totalProteinG = dto.totals?.proteinG ?: 0.0,
                        totalCarbsG = dto.totals?.carbsG ?: 0.0,
                        totalFatG = dto.totals?.fatG ?: 0.0,
                        items = dto.items.map { item ->
                            AiPhotoItem(
                                id = item.id,
                                foodId = item.foodId,
                                name = item.name,
                                matchedName = item.matchedName,
                                weightGrams = item.estimatedWeightGrams,
                                portionDescription = item.portionDescription ?: "1 porción",
                                preparationMethod = item.preparationMethod,
                                confidence = item.confidence,
                                calories = item.calories,
                                proteinG = item.proteinG,
                                carbsG = item.carbsG,
                                fatG = item.fatG,
                                candidates = item.candidates.map { c ->
                                    FoodCandidate(
                                        foodId = c.foodId,
                                        canonicalName = c.canonicalName,
                                        brandName = c.brandName,
                                        score = c.score,
                                        matchType = c.matchType,
                                        calories100g = c.calories100g,
                                        protein100g = c.protein100g,
                                        carbs100g = c.carbs100g,
                                        fat100g = c.fat100g
                                    )
                                }
                            )
                        }
                    )
                )
            } else {
                Result.Error(
                    exception = Exception("Error al obtener análisis (${response.code()})"),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error de red.")
        }
    }

    override suspend fun confirmAnalysis(
        analysisId: Long,
        date: String,
        mealType: String,
        items: List<ConfirmedPhotoItem>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val request = ConfirmPhotoAnalysisRequest(
                date = date,
                mealType = mealType,
                items = items.map { item ->
                    ConfirmPhotoItemRequest(
                        name = item.name,
                        foodId = item.foodId,
                        portionId = item.portionId,
                        quantity = item.quantity,
                        weightGrams = item.weightGrams,
                        calories = item.calories,
                        proteinG = item.proteinG,
                        carbsG = item.carbsG,
                        fatG = item.fatG
                    )
                }
            )

            val response = apiService.confirmAnalysis(analysisId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data.loggedEntriesCount)
            } else {
                Result.Error(
                    exception = Exception("Error al confirmar alimentos (${response.code()})"),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error al registrar en el diario.")
        }
    }

    override suspend fun parseMealText(
        text: String,
        locale: String,
        mealType: String
    ): Result<AiPhotoAnalysis> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.parseMealText(
                com.bsnutrition.app.core.network.dto.ParseTextRequest(
                    text = text,
                    locale = locale,
                    mealType = mealType
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!.data
                Result.Success(
                    AiPhotoAnalysis(
                        id = dto.id,
                        status = dto.status,
                        dishName = dto.dishName ?: "Registro de Texto / Voz",
                        summary = dto.summary ?: text,
                        confidenceScore = dto.confidenceScore ?: 0.95,
                        provider = dto.provider ?: "text_nlp",
                        totalCalories = dto.totals?.calories ?: 0,
                        totalProteinG = dto.totals?.proteinG ?: 0.0,
                        totalCarbsG = dto.totals?.carbsG ?: 0.0,
                        totalFatG = dto.totals?.fatG ?: 0.0,
                        items = dto.items.map { item ->
                            AiPhotoItem(
                                id = item.id,
                                foodId = item.foodId,
                                name = item.name,
                                matchedName = item.matchedName,
                                weightGrams = item.estimatedWeightGrams,
                                portionDescription = item.portionDescription ?: "1 porción",
                                preparationMethod = item.preparationMethod,
                                confidence = item.confidence,
                                calories = item.calories,
                                proteinG = item.proteinG,
                                carbsG = item.carbsG,
                                fatG = item.fatG,
                                candidates = item.candidates.map { c ->
                                    FoodCandidate(
                                        foodId = c.foodId,
                                        canonicalName = c.canonicalName,
                                        brandName = c.brandName,
                                        score = c.score,
                                        matchType = c.matchType,
                                        calories100g = c.calories100g,
                                        protein100g = c.protein100g,
                                        carbs100g = c.carbs100g,
                                        fat100g = c.fat100g
                                    )
                                }
                            )
                        }
                    )
                )
            } else {
                Result.Error(
                    exception = Exception("Error al procesar el texto (${response.code()})"),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Result.Error(e, e.localizedMessage ?: "Error al procesar la comida.")
        }
    }
}

