package com.igoy86.digitaltasbih.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.igoy86.digitaltasbih.R
import com.igoy86.digitaltasbih.databinding.ActivityMapsQiblaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.*

@AndroidEntryPoint
class MapsQiblaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsQiblaBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val KAABAH_LAT = 21.4225
        const val KAABAH_LNG = 39.8262
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, true)
		
        binding = ActivityMapsQiblaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar dengan back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Peta Kiblat"
		
		binding.toolbar.setTitleTextColor(Color.WHITE)
		
		val navIcon = binding.toolbar.navigationIcon
        if (navIcon != null) {
            DrawableCompat.setTint(DrawableCompat.wrap(navIcon), Color.WHITE)
        }

        // Terapkan warna tema
        val primaryHex = ThemeCache.load(this)
        val color = Color.parseColor(primaryHex)
        window.statusBarColor = color
        binding.toolbar.setBackgroundColor(color)
		
		// Set background tvDistanceMaps sesuai primary color dengan transparansi
        val primaryColor = Color.parseColor(ThemeCache.load(this))
        val overlayColor = Color.argb(230, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor))
        binding.tvDistanceMaps.setBackgroundColor(overlayColor)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
		val primaryColor = Color.parseColor(ThemeCache.load(this))

        // Map style sesuai dark/light mode
        val isDark = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() ==
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
        if (isDark) {
            try {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)
                )
            } catch (e: Exception) {
                // Fallback jika file tidak ada
            }
        }

        // Pin Ka'bah
        val kaabahLatLng = LatLng(KAABAH_LAT, KAABAH_LNG)
        googleMap.addMarker(
            MarkerOptions()
                .position(kaabahLatLng)
                .title("Ka'bah, Makkah")
                .icon(createLollipopPin(Color.parseColor("#FFC107")))
        )

        // Ambil lokasi user
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            googleMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { 
				location -> location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
					
					// Pin User — di DALAM lastLocation
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("Lokasi Kamu")
                            .icon(createLollipopPin(Color.parseColor(ThemeCache.load(this))))
                            .anchor(0.5f, 1f)
                    )
				
                    drawDashedGeodesicLine(userLatLng, kaabahLatLng)

                    val distance = calculateDistance(it.latitude, it.longitude)

                    // Info lengkap
                    binding.tvDistanceMaps.text = buildString {
                        append("📍 Lokasi kamu: ${"%.4f".format(it.latitude)}°, ${"%.4f".format(it.longitude)}°\n")
                        append("🕋 Ka'bah: ${KAABAH_LAT}°, ${KAABAH_LNG}°\n")
                        append("📏 Jarak ke Ka'bah: ${"%.0f".format(distance)} km")
                    }

                    val bounds = LatLngBounds.Builder()
                    .include(userLatLng)
                    .include(kaabahLatLng)
                    .build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
                }
            }
        }
    }

    private fun drawDashedGeodesicLine(from: LatLng, to: LatLng) {
        val primaryHex = ThemeCache.load(this)
        val color = Color.parseColor(primaryHex)

        // Generate titik-titik sepanjang great circle
        val points = generateGeodesicPoints(from, to, 80)

        // Gambar sebagai segmen putus-putus (setiap 2 titik skip 1)
        for (i in points.indices step 2) {
            if (i + 1 < points.size) {
                googleMap.addPolyline(
                    PolylineOptions()
                        .add(points[i], points[i + 1])
                        .color(color)
                        .width(5f)
                        .geodesic(false)
                )
            }
        }
    }

    // Generate titik-titik di sepanjang great circle (geodesic)
    private fun generateGeodesicPoints(from: LatLng, to: LatLng, numPoints: Int): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val lat1 = Math.toRadians(from.latitude)
        val lng1 = Math.toRadians(from.longitude)
        val lat2 = Math.toRadians(to.latitude)
        val lng2 = Math.toRadians(to.longitude)

        val d = 2 * asin(
            sqrt(
                sin((lat2 - lat1) / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin((lng2 - lng1) / 2).pow(2)
            )
        )

        for (i in 0..numPoints) {
            val f = i.toDouble() / numPoints
            val A = sin((1 - f) * d) / sin(d)
            val B = sin(f * d) / sin(d)
            val x = A * cos(lat1) * cos(lng1) + B * cos(lat2) * cos(lng2)
            val y = A * cos(lat1) * sin(lng1) + B * cos(lat2) * sin(lng2)
            val z = A * sin(lat1) + B * sin(lat2)
            val lat = atan2(z, sqrt(x.pow(2) + y.pow(2)))
            val lng = atan2(y, x)
            points.add(LatLng(Math.toDegrees(lat), Math.toDegrees(lng)))
        }
        return points
    }

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
	
	private fun createLollipopPin(color: Int): BitmapDescriptor {
        val size = 80
        val stickWidth = 6
        val circleRadius = 28
        val bitmap = Bitmap.createBitmap(size, size * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Batang lollipop
        paint.color = color
        paint.strokeWidth = stickWidth.toFloat()
        paint.style = Paint.Style.STROKE
        canvas.drawLine(
            size / 2f, circleRadius * 2f,
            size / 2f, size * 2f - 4f,
            paint
        )

        // Lingkaran luar (putih)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, circleRadius.toFloat(), circleRadius.toFloat(), paint)

        // Lingkaran dalam (primary color)
        paint.color = color
        canvas.drawCircle(size / 2f, circleRadius.toFloat(), (circleRadius - 8).toFloat(), paint)

        // Titik tengah putih
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, circleRadius.toFloat(), 6f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}