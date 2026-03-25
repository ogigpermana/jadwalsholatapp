package com.igoy86.digitaltasbih.data.model

data class QiblaData(
    val userLat: Double,
    val userLng: Double,
    val qiblaAngle: Float,       // sudut arah kiblat dari utara (0-360)
    val compassBearing: Float,   // arah kompas saat ini dari sensor
    val distanceKm: Double       // jarak ke Ka'bah dalam km
)