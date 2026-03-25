package com.igoy86.digitaltasbih.domain.repository

import com.igoy86.digitaltasbih.data.model.QiblaData
import kotlinx.coroutines.flow.Flow

interface QiblaRepository {
    fun getQiblaDirection(): Flow<QiblaData>
    fun startSensor()
    fun stopSensor()
}