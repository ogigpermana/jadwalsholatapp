package com.igoy86.digitaltasbih.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.AlarmManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.igoy86.digitaltasbih.data.model.PrayerTime
import com.igoy86.digitaltasbih.databinding.FragmentPrayerScheduleBinding
import com.igoy86.digitaltasbih.ui.PrayerAdapter
import com.igoy86.digitaltasbih.ui.viewmodel.PrayerScheduleViewModel
import com.igoy86.digitaltasbih.ui.viewmodel.ThemeViewModel
import android.os.Build
import com.igoy86.digitaltasbih.notification.PrayerNotificationChannel
import com.igoy86.digitaltasbih.notification.PrayerNotificationScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.igoy86.digitaltasbih.data.local.SettingsDataStore
import javax.inject.Inject
import android.content.Intent
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class PrayerScheduleFragment : Fragment() {

    private var _binding: FragmentPrayerScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrayerScheduleViewModel by viewModels()
	private val notifPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or not, lanjut saja */ }
	private val themeViewModel: ThemeViewModel by activityViewModels()
	@Inject
    lateinit var settingsDataStore: SettingsDataStore

    private val adapter = PrayerAdapter { prayerTime, enabled ->
        onAlarmToggle(prayerTime, enabled)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.loadPrayerTimes()
        else showError("Izin lokasi diperlukan untuk menampilkan jadwal sholat")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrayerScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPrayerTimes.adapter = adapter

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())
		val textColor = resolveAttr(com.google.android.material.R.attr.colorOnBackground)
        binding.tvLocation.setTextColor(textColor)
        binding.tvDate.setTextColor(textColor)
        binding.tvHijriDate.setTextColor(textColor)

        binding.btnRetry.setOnClickListener { checkPermissionAndLoad() }

        observeUiState()
        checkPermissionAndLoad()
		
		lifecycleScope.launch {
            val themeViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[ThemeViewModel::class.java]
            themeViewModel.themeState.collect { state ->
                adapter.updateThemeColor(state.primaryColor, state.darkColor)
            }
        }
		
		requestNotifPermissionIfNeeded()
        PrayerNotificationChannel.create(requireContext())
    }

    private fun checkPermissionAndLoad() {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarse = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadPrayerTimes()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is PrayerScheduleViewModel.UiState.Loading -> showLoading()
                    is PrayerScheduleViewModel.UiState.Success -> {
                        showContent()
                        adapter.submitList(state.info.prayers)
                        binding.tvHijriDate.text = state.info.hijriDate
                        binding.tvPrayerStatus.text = state.info.statusText
                        updateLocationLabel()
						applyAdapterTheme()
						scheduleNotifications(state.info.prayers)
                    }
                    is PrayerScheduleViewModel.UiState.Error -> showError(state.message)
                    is PrayerScheduleViewModel.UiState.LocationPermissionRequired -> {
                        checkPermissionAndLoad()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationLabel() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fusedClient = LocationServices
                    .getFusedLocationProviderClient(requireContext())
                val location = fusedClient.lastLocation.await() ?: return@launch
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val city = addresses?.firstOrNull()?.subAdminArea
                    ?: addresses?.firstOrNull()?.adminArea
                    ?: "Lokasi ditemukan"
                binding.tvLocation.text = "📍 $city"
            } catch (e: Exception) {
                binding.tvLocation.text = "📍 Lokasi ditemukan"
            }
        }
    }
	
	private fun applyAdapterTheme() {
       val state = themeViewModel.themeState.value
       adapter.updateThemeColor(state.primaryColor, state.darkColor)
    }

    private fun onAlarmToggle(prayerTime: PrayerTime, enabled: Boolean) {
        val current = adapter.currentList.toMutableList()
        val index = current.indexOfFirst { it.key == prayerTime.key }
        if (index != -1) {
            current[index] = current[index].copy(notifEnabled = enabled)
            adapter.submitList(current.toList())

            // Reschedule alarm
            if (enabled) {
                PrayerNotificationScheduler.schedule(requireContext(), current[index], index)
            } else {
                PrayerNotificationScheduler.cancel(requireContext(), index)
            }

            // Simpan state ke DataStore (persisten)
            viewLifecycleOwner.lifecycleScope.launch {
                settingsDataStore.setPrayerNotifEnabled(prayerTime.key, enabled)

                // Update active keys
                val activeKeys = current
                    .filter { it.notifEnabled }
                    .map { it.key }
                settingsDataStore.saveActivePrayerKeys(activeKeys)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.rvPrayerTimes.visibility = View.GONE
    }

    private fun showContent() {
        binding.progressBar.visibility = View.GONE
        binding.layoutError.visibility = View.GONE
        binding.rvPrayerTimes.visibility = View.VISIBLE
		
		// Fix warna teks header ikut theme
        val textColor = resolveAttr(com.google.android.material.R.attr.colorOnBackground)
        binding.tvLocation.setTextColor(textColor)
        binding.tvDate.setTextColor(textColor)
        binding.tvHijriDate.setTextColor(textColor)
    }
	
	private fun resolveAttr(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.rvPrayerTimes.visibility = View.GONE
        binding.tvError.text = message
    }
	
	private fun requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
	
	private fun scheduleNotifications(prayers: List<PrayerTime>) {
    val alarmManager = requireContext().getSystemService(android.app.AlarmManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
            return
        }
    }

    viewLifecycleOwner.lifecycleScope.launch {
        val updatedPrayers = prayers.map { prayer ->
            val enabled = settingsDataStore.getPrayerNotifEnabled(prayer.key).first()
            prayer.copy(notifEnabled = enabled)
        }

        // Update lastPrayerInfo di ViewModel supaya ticker tidak override notifEnabled
        viewModel.updateNotifState(updatedPrayers)

        // Schedule alarm
        PrayerNotificationScheduler.scheduleAll(requireContext(), updatedPrayers)
    }
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}