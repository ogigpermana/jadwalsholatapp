package com.igoy86.digitaltasbih.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.igoy86.digitaltasbih.databinding.FragmentQiblaCompassBinding
import com.igoy86.digitaltasbih.ui.MapsQiblaActivity
import com.igoy86.digitaltasbih.ui.viewmodel.QiblaUiState
import com.igoy86.digitaltasbih.ui.viewmodel.QiblaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QiblaCompassFragment : Fragment() {

    private var _binding: FragmentQiblaCompassBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QiblaViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            startCompass()
        } else {
            Toast.makeText(requireContext(), "Izin lokasi diperlukan untuk arah kiblat yang akurat", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQiblaCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkLocationPermission()

        // Tap kompas → buka MapsQiblaActivity
        binding.compassView.setOnClickListener {
            val intent = Intent(requireContext(), MapsQiblaActivity::class.java)
            startActivity(intent)
        }

        observeQiblaState()
    }

    private fun checkLocationPermission() {
        val fine   = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            startCompass()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startCompass() {
        viewModel.startQibla()
    }

    private fun observeQiblaState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.qiblaState.collect { state ->
                when (state) {
                    is QiblaUiState.Loading -> {
                        binding.tvQiblaAngle.text = "Mendeteksi arah kiblat..."
                        binding.tvCompassBearing.text = ""
                    }
                    is QiblaUiState.Success -> {
                        val data = state.data
                        binding.compassView.qiblaAngle    = data.qiblaAngle
                        binding.compassView.compassBearing = data.compassBearing
                        binding.compassView.updateTheme()

                        binding.tvQiblaAngle.text = "Arah Kiblat: ${"%.1f".format(data.qiblaAngle)}° dari Utara"
                        binding.tvCompassBearing.text = "Arah Kompas: ${"%.1f".format(data.compassBearing)}°"
                        binding.tvDistance.text = "Jarak ke Ka'bah: ${"%.0f".format(data.distanceKm)} km"
                    }
                    is QiblaUiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}