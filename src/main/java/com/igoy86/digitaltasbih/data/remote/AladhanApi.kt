package com.igoy86.digitaltasbih.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface AladhanApi {

    @GET("v1/timings")
    suspend fun getPrayerTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 11, // KEMENAG Indonesia
        @Query("timestamp") timestamp: Long = System.currentTimeMillis() / 1000,
    ): PrayerResponse
}