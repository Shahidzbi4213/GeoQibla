package com.shahid.tech.qibla

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QiblaStateMachineTest {
    @Test
    fun permissionStatesTakePriorityBeforeLocationAndSensorState() {
        val config = QiblaConfig()

        assertEquals(
            QiblaUiState.PERMISSION_REQUIRED,
            resolve(location(access = QiblaLocationAccess.NOT_DETERMINED), config = config),
        )
        assertEquals(
            QiblaUiState.PERMISSION_DENIED,
            resolve(location(access = QiblaLocationAccess.DENIED), config = config),
        )
        assertEquals(
            QiblaUiState.PERMISSION_PERMANENTLY_DENIED,
            resolve(location(access = QiblaLocationAccess.PERMANENTLY_DENIED), config = config),
        )
    }

    @Test
    fun grantedLocationStatesResolveInExpectedPriority() {
        val config = QiblaConfig()

        assertEquals(
            QiblaUiState.LOCATION_DISABLED,
            resolve(
                location = location(isLocationEnabled = false),
                compass = readyCompass(),
                config = config,
            ),
        )
        assertEquals(
            QiblaUiState.LOCATING,
            resolve(location = location(fix = null), compass = readyCompass(), config = config),
        )
        assertEquals(
            QiblaUiState.SENSOR_UNAVAILABLE,
            resolve(location = location(), compass = QiblaCompassState(), config = config),
        )
        assertEquals(
            QiblaUiState.TILTED,
            resolve(
                location = location(),
                compass = readyCompass().copy(isTilted = true),
                config = config,
            ),
        )
    }

    @Test
    fun calibrationCanBeDismissedWithoutChangingSensorReadings() {
        val config = QiblaConfig()
        val compass = readyCompass().copy(magneticFieldMicrotesla = 90f)

        assertEquals(
            QiblaUiState.CALIBRATION_NEEDED,
            resolve(
                location = location(),
                compass = compass,
                sensorAccuracy = QiblaSensorAccuracy.LOW,
                isCalibrationDismissed = false,
                config = config,
            ),
        )
        assertEquals(
            QiblaUiState.READY,
            resolve(
                location = location(),
                compass = compass,
                sensorAccuracy = QiblaSensorAccuracy.LOW,
                isCalibrationDismissed = true,
                config = config,
            ),
        )
    }

    @Test
    fun alignedStateRequiresStableDurationAndNotifiesOnce() {
        val tracker = QiblaAlignmentTracker()
        val config = QiblaConfig(stableAlignedDurationMillis = 500L)

        val first = tracker.update(2f, timestampMillis = 1_000L, config = config)
        assertTrue(first.isNear)
        assertFalse(first.isAligned)
        assertFalse(first.shouldNotify)

        val stable = tracker.update(1f, timestampMillis = 1_500L, config = config)
        assertTrue(stable.isAligned)
        assertTrue(stable.shouldNotify)

        val repeated = tracker.update(1f, timestampMillis = 1_700L, config = config)
        assertTrue(repeated.isAligned)
        assertFalse(repeated.shouldNotify)

        val left = tracker.update(12f, timestampMillis = 1_800L, config = config)
        assertFalse(left.isAligned)
        assertFalse(left.isNear)

        val alignedAgain = tracker.update(0f, timestampMillis = 2_300L, config = config)
        assertFalse(alignedAgain.isAligned)
        assertFalse(alignedAgain.shouldNotify)
    }

    private fun resolve(
        location: QiblaLocationState,
        compass: QiblaCompassState = QiblaCompassState(),
        sensorAccuracy: QiblaSensorAccuracy = QiblaSensorAccuracy.HIGH,
        isCalibrationDismissed: Boolean = false,
        config: QiblaConfig,
    ): QiblaUiState = QiblaUiStateResolver.resolve(
        isStarted = true,
        location = location,
        compass = compass,
        sensorAccuracy = sensorAccuracy,
        errorMessage = null,
        isCalibrationDismissed = isCalibrationDismissed,
        config = config,
    )

    private fun location(
        access: QiblaLocationAccess = QiblaLocationAccess.GRANTED,
        fix: QiblaLocationFix? = QiblaLocationFix(latitude = 21.3891, longitude = 39.8579),
        isLocationEnabled: Boolean = true,
    ): QiblaLocationState = QiblaLocationState(
        access = access,
        fix = fix,
        isLocationEnabled = isLocationEnabled,
    )

    private fun readyCompass(): QiblaCompassState = QiblaCompassState(
        qiblaBearingDegrees = 45f,
        azimuthDegrees = 30f,
        directionToQiblaDegrees = 15f,
    )
}
