package com.igoy86.digitaltasbih.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.igoy86.digitaltasbih.data.model.QiblaData
import com.igoy86.digitaltasbih.domain.repository.QiblaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class QiblaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : QiblaRepository {

    // Koordinat Ka'bah, Makkah
    companion object {
        const val KAABAH_LAT = 21.4225
        const val KAABAH_LNG = 39.8262
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometerValues = FloatArray(3)
    private var magnetometerValues = FloatArray(3)
    private var currentBearing = 0f
    private var sensorListener: SensorEventListener? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun getQiblaDirection(): Flow<QiblaData> = callbackFlow {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        var userLat = 0.0
        var userLng = 0.0
        var locationReady = false

        // Ambil last known location
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLat = it.latitude
                    userLng = it.longitude
                    locationReady = true
                }
            }
        } catch (e: SecurityException) {
            // Permission belum diberikan
        }

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER ->
                        accelerometerValues = event.values.clone()
                    Sensor.TYPE_MAGNETIC_FIELD ->
                        magnetometerValues = event.values.clone()
                }

                val rotationMatrix = FloatArray(9)
                val inclinationMatrix = FloatArray(9)
                val success = SensorManager.getRotationMatrix(
                    rotationMatrix, inclinationMatrix,
                    accelerometerValues, magnetometerValues
                )

                if (success) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    // orientation[0] = azimuth dalam radian
                    currentBearing = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()

                    if (locationReady) {
                        val qiblaAngle = calculateQiblaAngle(userLat, userLng)
                        val distance = calculateDistance(userLat, userLng)
                        trySend(
                            QiblaData(
                                userLat = userLat,
                                userLng = userLng,
                                qiblaAngle = qiblaAngle,
                                compassBearing = currentBearing,
                                distanceKm = distance
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    override fun startSensor() {
        // Handled di dalam flow
    }

    override fun stopSensor() {
        sensorListener?.let { sensorManager.unregisterListener(it) }
    }

    // Hitung sudut kiblat menggunakan formula geodesik
    private fun calculateQiblaAngle(lat: Double, lng: Double): Float {
        val userLatRad = Math.toRadians(lat)
        val userLngRad = Math.toRadians(lng)
        val kaabahLatRad = Math.toRadians(KAABAH_LAT)
        val kaabahLngRad = Math.toRadians(KAABAH_LNG)

        val deltaLng = kaabahLngRad - userLngRad

        val y = sin(deltaLng) * cos(kaabahLatRad)
        val x = cos(userLatRad) * sin(kaabahLatRad) -
                sin(userLatRad) * cos(kaabahLatRad) * cos(deltaLng)

        val angle = Math.toDegrees(atan2(y, x))
        return ((angle + 360) % 360).toFloat()
    }

    // Hitung jarak ke Ka'bah (Haversine formula)
    private fun calculateDistance(lat: Double, lng: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(KAABAH_LAT - lat)
        val dLng = Math.toRadians(KAABAH_LNG - lng)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABAH_LAT)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}