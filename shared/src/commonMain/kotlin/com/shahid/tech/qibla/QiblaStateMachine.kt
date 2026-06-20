package com.shahid.tech.qibla

import kotlin.math.abs

internal class QiblaAlignmentTracker {
    private var alignedSinceMillis: Long? = null
    private var callbackDelivered = false

    fun update(
        directionToQiblaDegrees: Float?,
        timestampMillis: Long,
        config: QiblaConfig,
    ): QiblaAlignmentResult {
        val absoluteDirection = directionToQiblaDegrees?.let { abs(it) }
        val isNear = absoluteDirection != null && absoluteDirection <= config.nearDegrees
        val isWithinAlignedThreshold =
            absoluteDirection != null && absoluteDirection <= config.alignedDegrees

        if (!isWithinAlignedThreshold) {
            alignedSinceMillis = null
            callbackDelivered = false
            return QiblaAlignmentResult(isNear = isNear, isAligned = false, shouldNotify = false)
        }

        val startedAt = alignedSinceMillis ?: timestampMillis.also {
            alignedSinceMillis = it
        }
        val isStable = timestampMillis - startedAt >= config.stableAlignedDurationMillis
        val shouldNotify = isStable && !callbackDelivered
        if (shouldNotify) callbackDelivered = true

        return QiblaAlignmentResult(
            isNear = isNear,
            isAligned = isStable,
            shouldNotify = shouldNotify,
        )
    }

    fun reset() {
        alignedSinceMillis = null
        callbackDelivered = false
    }
}

internal data class QiblaAlignmentResult(
    val isNear: Boolean,
    val isAligned: Boolean,
    val shouldNotify: Boolean,
)

internal object QiblaUiStateResolver {
    fun resolve(
        isStarted: Boolean,
        location: QiblaLocationState,
        compass: QiblaCompassState,
        sensorAccuracy: QiblaSensorAccuracy,
        errorMessage: String?,
        isCalibrationDismissed: Boolean,
        config: QiblaConfig,
    ): QiblaUiState {
        if (errorMessage != null) return QiblaUiState.ERROR
        if (!isStarted) return QiblaUiState.IDLE

        return when (location.access) {
            QiblaLocationAccess.UNKNOWN,
            QiblaLocationAccess.NOT_DETERMINED,
            -> QiblaUiState.PERMISSION_REQUIRED

            QiblaLocationAccess.DENIED -> QiblaUiState.PERMISSION_DENIED
            QiblaLocationAccess.PERMANENTLY_DENIED -> QiblaUiState.PERMISSION_PERMANENTLY_DENIED
            QiblaLocationAccess.GRANTED -> resolveGrantedState(
                location = location,
                compass = compass,
                sensorAccuracy = sensorAccuracy,
                isCalibrationDismissed = isCalibrationDismissed,
                config = config,
            )
        }
    }

    private fun resolveGrantedState(
        location: QiblaLocationState,
        compass: QiblaCompassState,
        sensorAccuracy: QiblaSensorAccuracy,
        isCalibrationDismissed: Boolean,
        config: QiblaConfig,
    ): QiblaUiState {
        if (!location.isLocationEnabled) return QiblaUiState.LOCATION_DISABLED
        if (location.fix == null) return QiblaUiState.LOCATING
        if (compass.azimuthDegrees == null) return QiblaUiState.SENSOR_UNAVAILABLE
        if (compass.isTilted) return QiblaUiState.TILTED

        val magneticField = compass.magneticFieldMicrotesla
        val hasMagneticWarning = magneticField != null &&
            (magneticField < config.magneticFieldMinMicrotesla ||
                magneticField > config.magneticFieldMaxMicrotesla)
        val needsCalibration = sensorAccuracy == QiblaSensorAccuracy.UNRELIABLE ||
            sensorAccuracy == QiblaSensorAccuracy.LOW ||
            hasMagneticWarning

        if (needsCalibration && !isCalibrationDismissed) {
            return QiblaUiState.CALIBRATION_NEEDED
        }
        if (compass.isAligned) return QiblaUiState.ALIGNED
        if (compass.isNearQibla) return QiblaUiState.NEAR_QIBLA
        return QiblaUiState.READY
    }
}
