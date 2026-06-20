package com.shahid.tech.qibla

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class DefaultQiblaController(
    private val coroutineScope: CoroutineScope,
    private val services: QiblaPlatformServices,
    initialConfig: QiblaConfig,
) : QiblaController {
    private val mutableState = MutableStateFlow(
        QiblaState(
            location = QiblaLocationState(access = services.locationAccess.value),
        ),
    )
    private val alignmentTracker = QiblaAlignmentTracker()

    private var config = initialConfig
    private var permissionJob: Job? = null
    private var locationJob: Job? = null
    private var orientationJob: Job? = null
    private var latestOrientation: QiblaOrientationSnapshot? = null
    private var isCalibrationDismissed = false

    override val state: StateFlow<QiblaState> = mutableState.asStateFlow()

    override fun start() {
        if (mutableState.value.isStarted) return

        updateState {
            it.copy(
                isStarted = true,
                errorMessage = null,
                uiState = QiblaUiStateResolver.resolve(
                    isStarted = true,
                    location = it.location,
                    compass = it.compass,
                    sensorAccuracy = it.sensorAccuracy,
                    errorMessage = null,
                    isCalibrationDismissed = isCalibrationDismissed,
                    config = config,
                ),
            )
        }
        startPermissionObserver()
        startLocationObserver()
        startOrientationObserver()
    }

    override fun stop() {
        permissionJob?.cancel()
        locationJob?.cancel()
        orientationJob?.cancel()
        permissionJob = null
        locationJob = null
        orientationJob = null
        latestOrientation = null
        alignmentTracker.reset()
        updateState {
            it.copy(
                isStarted = false,
                compass = it.compass.copy(isNearQibla = false, isAligned = false),
                uiState = QiblaUiState.IDLE,
            )
        }
    }

    override fun retryLocation() {
        locationJob?.cancel()
        locationJob = null
        isCalibrationDismissed = false
        alignmentTracker.reset()
        if (mutableState.value.isStarted) startLocationObserver()
    }

    override fun requestPermission() {
        updateState { it.copy(uiState = QiblaUiState.REQUESTING_PERMISSION) }
        services.requestPermission()
    }

    override fun openLocationSettings() {
        services.openLocationSettings()
    }

    override fun openAppSettings() {
        services.openAppSettings()
    }

    override fun dismissCalibration() {
        isCalibrationDismissed = true
        reduceState()
    }

    fun updateConfig(nextConfig: QiblaConfig) {
        if (config == nextConfig) return
        config = nextConfig
        alignmentTracker.reset()
        reduceState()
    }

    private fun startPermissionObserver() {
        if (permissionJob != null) return
        permissionJob = coroutineScope.launch {
            services.locationAccess.collectLatest { access ->
                val currentLocation = mutableState.value.location
                updateLocation(
                    currentLocation.copy(access = access),
                    errorMessage = null,
                )
            }
        }
    }

    private fun startLocationObserver() {
        if (locationJob != null) return
        locationJob = coroutineScope.launch {
            services.observeLocation(config).collectLatest { snapshot ->
                val fix = snapshot.fix
                val label = snapshot.label ?: fix?.let { formatLocationLabel(it) }
                updateLocation(
                    QiblaLocationState(
                        access = snapshot.access,
                        fix = fix,
                        isLocationEnabled = snapshot.isLocationEnabled,
                        isResolvingAddress = snapshot.isResolvingAddress,
                        label = label,
                    ),
                    errorMessage = snapshot.errorMessage,
                )
            }
        }
    }

    private fun startOrientationObserver() {
        if (orientationJob != null) return
        orientationJob = coroutineScope.launch {
            services.observeOrientation(config).collectLatest { snapshot ->
                latestOrientation = snapshot
                updateCompassFromLatestInputs()
            }
        }
    }

    private fun updateLocation(location: QiblaLocationState, errorMessage: String?) {
        val fix = location.fix
        val compass = if (fix == null) {
            mutableState.value.compass.copy(
                qiblaBearingDegrees = null,
                directionToQiblaDegrees = null,
                distanceToKaabaMeters = null,
                isNearQibla = false,
                isAligned = false,
            )
        } else {
            mutableState.value.compass.copy(
                qiblaBearingDegrees = QiblaMath.bearingToQibla(fix.latitude, fix.longitude),
                distanceToKaabaMeters = QiblaMath.distanceToKaabaMeters(
                    fix.latitude,
                    fix.longitude,
                ),
            )
        }

        updateState {
            it.copy(
                location = location,
                compass = compass,
                errorMessage = errorMessage,
            )
        }
        updateCompassFromLatestInputs()
    }

    private fun updateCompassFromLatestInputs() {
        val orientation = latestOrientation
        val current = mutableState.value
        val fix = current.location.fix
        val qiblaBearing = current.compass.qiblaBearingDegrees

        if (orientation == null) {
            reduceState()
            return
        }

        val azimuth = orientation.azimuthDegrees?.let { degrees ->
            when (orientation.azimuthReference) {
                QiblaAzimuthReference.MAGNETIC_NORTH -> QiblaMath.magneticToTrueAzimuth(
                    magneticAzimuthDegrees = degrees,
                    declinationDegrees = fix?.declinationDegrees ?: 0f,
                )

                QiblaAzimuthReference.TRUE_NORTH -> QiblaMath.normalizeDegrees(degrees)
            }
        }
        val smoothedAzimuth = azimuth?.let {
            QiblaMath.smoothDegrees(
                previousDegrees = current.compass.azimuthDegrees,
                targetDegrees = it,
                factor = config.smoothingFactor,
            )
        }
        val direction = if (smoothedAzimuth != null && qiblaBearing != null) {
            QiblaMath.shortestAngularDistance(smoothedAzimuth, qiblaBearing)
        } else {
            null
        }
        val alignment = alignmentTracker.update(
            directionToQiblaDegrees = direction,
            timestampMillis = currentTimeMillis(),
            config = config,
        )
        val isTilted = orientation.tiltDegrees > config.tiltLimitDegrees

        updateState {
            it.copy(
                compass = it.compass.copy(
                    azimuthDegrees = smoothedAzimuth,
                    directionToQiblaDegrees = direction,
                    tiltDegrees = orientation.tiltDegrees,
                    magneticFieldMicrotesla = orientation.magneticFieldMicrotesla,
                    isNearQibla = alignment.isNear,
                    isAligned = alignment.isAligned,
                    isTilted = isTilted,
                ),
                sensorAccuracy = orientation.accuracy,
                orientationSource = orientation.source,
            )
        }

        if (alignment.shouldNotify) {
            config.onAligned?.invoke()
            if (config.hapticsEnabled) services.performAlignmentHaptic()
        }
    }

    private fun reduceState() {
        updateState { current ->
            current.copy(
                uiState = QiblaUiStateResolver.resolve(
                    isStarted = current.isStarted,
                    location = current.location,
                    compass = current.compass,
                    sensorAccuracy = current.sensorAccuracy,
                    errorMessage = current.errorMessage,
                    isCalibrationDismissed = isCalibrationDismissed,
                    config = config,
                ),
            )
        }
    }

    private fun updateState(transform: (QiblaState) -> QiblaState) {
        mutableState.value = transform(mutableState.value).let { next ->
            next.copy(
                uiState = QiblaUiStateResolver.resolve(
                    isStarted = next.isStarted,
                    location = next.location,
                    compass = next.compass,
                    sensorAccuracy = next.sensorAccuracy,
                    errorMessage = next.errorMessage,
                    isCalibrationDismissed = isCalibrationDismissed,
                    config = config,
                ),
            )
        }
    }

    private fun formatLocationLabel(fix: QiblaLocationFix): String =
        "${fix.latitude.formatCoordinate()}, ${fix.longitude.formatCoordinate()}"
}

internal expect fun currentTimeMillis(): Long

private fun Double.formatCoordinate(): String {
    val rounded = (this * 10_000.0).toInt() / 10_000.0
    return rounded.toString()
}
