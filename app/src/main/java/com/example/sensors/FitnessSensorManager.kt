package com.example.sensors

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class SensorSnapshot(
    val hasHardwareStepSensor: Boolean = false,
    val hasGpsPermission: Boolean = false,
    val hasGpsSignal: Boolean = false,
    val gpsAccuracyMeters: Float? = null,
    val currentSpeedMps: Float = 0f,
    val elevationGainMeters: Double = 0.0,
    val sessionHardwareSteps: Int = 0,
    val sessionGpsDistanceKm: Double = 0.0,
    val recordedCoordinates: List<Pair<Double, Double>> = emptyList()
)

class FitnessSensorManager(context: Context) : SensorEventListener, LocationListener {
    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appContext)

    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var initialStepCount: Float? = null
    private var pausedStepOffset: Int = 0
    private var lastPauseStepTotal: Int? = null
    private var lastLocation: Location? = null
    private var lastAltitude: Double? = null
    private var isTrackingSession = false
    private var isPausedSession = false
    private var usingFusedClient = false

    private val _snapshot = MutableStateFlow(
        SensorSnapshot(
            hasHardwareStepSensor = stepSensor != null,
            hasGpsPermission = checkLocationPermission()
        )
    )
    val snapshot: StateFlow<SensorSnapshot> = _snapshot.asStateFlow()

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (loc in result.locations) {
                handleIncomingLocation(loc)
            }
        }
    }

    fun checkLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun checkActivityPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun refreshPermissionState() {
        _snapshot.value = _snapshot.value.copy(
            hasGpsPermission = checkLocationPermission()
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun getBestKnownCoordinates(): Pair<Double, Double>? {
        val recorded = _snapshot.value.recordedCoordinates.lastOrNull()
        if (recorded != null) return recorded
        if (!checkLocationPermission()) return null

        val fusedLoc: Location? = try {
            suspendCancellableCoroutine { cont ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }
        } catch (_: Exception) {
            null
        }
        if (fusedLoc != null) {
            return fusedLoc.latitude to fusedLoc.longitude
        }

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        for (provider in providers) {
            try {
                val loc = locationManager?.getLastKnownLocation(provider)
                if (loc != null) return loc.latitude to loc.longitude
            } catch (_: Exception) {
            }
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        isTrackingSession = true
        isPausedSession = false
        initialStepCount = null
        pausedStepOffset = 0
        lastPauseStepTotal = null
        lastLocation = null
        lastAltitude = null

        val gpsGranted = checkLocationPermission()
        _snapshot.value = _snapshot.value.copy(
            hasGpsPermission = gpsGranted,
            hasGpsSignal = false,
            gpsAccuracyMeters = null,
            currentSpeedMps = 0f,
            elevationGainMeters = 0.0,
            sessionHardwareSteps = 0,
            sessionGpsDistanceKm = 0.0,
            recordedCoordinates = emptyList()
        )

        if (stepSensor != null && checkActivityPermission()) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (gpsGranted) {
            startLocationUpdates()
        }
    }

    fun pauseTracking() {
        if (!isTrackingSession) return
        isPausedSession = true
        lastLocation = null
        _snapshot.value = _snapshot.value.copy(currentSpeedMps = 0f)
    }

    fun resumeTracking() {
        if (!isTrackingSession) return
        isPausedSession = false
        lastLocation = null
        lastPauseStepTotal = null
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(2f)
            .build()

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { cached ->
                if (cached != null && isTrackingSession && _snapshot.value.recordedCoordinates.isEmpty()) {
                    handleIncomingLocation(cached)
                }
            }
            fusedLocationClient.requestLocationUpdates(
                request,
                fusedCallback,
                Looper.getMainLooper()
            )
            usingFusedClient = true
        } catch (_: Exception) {
            usingFusedClient = false
            startLegacyLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLegacyLocationUpdates() {
        if (locationManager == null) return
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1500L,
                    2f,
                    this,
                    Looper.getMainLooper()
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    4f,
                    this,
                    Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {
        }
    }

    fun stopTracking() {
        isTrackingSession = false
        isPausedSession = false
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {
        }
        if (usingFusedClient) {
            try {
                fusedLocationClient.removeLocationUpdates(fusedCallback)
            } catch (_: Exception) {
            }
            usingFusedClient = false
        }
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {
        }
        _snapshot.value = _snapshot.value.copy(currentSpeedMps = 0f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isTrackingSession || event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
        val totalSinceBoot = event.values.firstOrNull() ?: return
        if (initialStepCount == null) {
            initialStepCount = totalSinceBoot
        }
        val rawDelta = (totalSinceBoot - (initialStepCount ?: totalSinceBoot)).toInt().coerceAtLeast(0)
        if (isPausedSession) {
            if (lastPauseStepTotal == null) {
                lastPauseStepTotal = rawDelta
            } else {
                val pauseSteps = (rawDelta - (lastPauseStepTotal ?: rawDelta)).coerceAtLeast(0)
                pausedStepOffset += pauseSteps
                lastPauseStepTotal = rawDelta
            }
            return
        }
        val activeSteps = (rawDelta - pausedStepOffset).coerceAtLeast(0)
        _snapshot.value = _snapshot.value.copy(sessionHardwareSteps = activeSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onLocationChanged(location: Location) {
        handleIncomingLocation(location)
    }

    private fun handleIncomingLocation(location: Location) {
        if (!isTrackingSession) return
        val accuracy = if (location.hasAccuracy()) location.accuracy else 12f

        if (isPausedSession) {
            _snapshot.value = _snapshot.value.copy(
                hasGpsSignal = true,
                gpsAccuracyMeters = accuracy,
                currentSpeedMps = 0f
            )
            return
        }

        // Ignore very inaccurate fixes once we already have a position
        if (accuracy > 35f && _snapshot.value.recordedCoordinates.isNotEmpty()) {
            _snapshot.value = _snapshot.value.copy(
                hasGpsSignal = true,
                gpsAccuracyMeters = accuracy
            )
            return
        }

        val previous = lastLocation
        var newDistanceKm = _snapshot.value.sessionGpsDistanceKm
        var updatedCoords = _snapshot.value.recordedCoordinates
        var speedMps = if (location.hasSpeed() && location.speed >= 0.4f) location.speed else 0f
        var elevationGain = _snapshot.value.elevationGainMeters

        if (previous == null) {
            lastLocation = location
            if (location.hasAltitude()) {
                lastAltitude = location.altitude
            }
            if (updatedCoords.isEmpty()) {
                updatedCoords = updatedCoords + (location.latitude to location.longitude)
            }
        } else {
            val meters = previous.distanceTo(location)
            val dtSec = ((location.time - previous.time) / 1000f).coerceAtLeast(0.5f)
            val minNoiseMeters = (accuracy * 0.35f).coerceIn(2f, 5f)
            val impliedSpeed = meters / dtSec

            // Filter standing jitter and unrealistic GPS jumps (> 14 m/s ~ 50 km/h)
            if (meters >= minNoiseMeters && impliedSpeed <= 14f) {
                newDistanceKm += meters / 1000.0
                if (speedMps <= 0.1f) {
                    speedMps = impliedSpeed
                }
                lastLocation = location
                updatedCoords = updatedCoords + (location.latitude to location.longitude)

                if (location.hasAltitude()) {
                    val prevAlt = lastAltitude
                    if (prevAlt != null) {
                        val altDiff = location.altitude - prevAlt
                        if (altDiff in 0.8..15.0) {
                            elevationGain += altDiff
                        }
                    }
                    lastAltitude = location.altitude
                }
            }
        }

        _snapshot.value = _snapshot.value.copy(
            hasGpsSignal = true,
            gpsAccuracyMeters = accuracy,
            currentSpeedMps = speedMps.coerceIn(0f, 14f),
            elevationGainMeters = elevationGain,
            sessionGpsDistanceKm = newDistanceKm,
            recordedCoordinates = updatedCoords
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
}
