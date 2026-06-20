package com.shahid.tech.qibla

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QiblaMathTest {
    @Test
    fun computesKnownCityQiblaBearings() {
        assertClose(expected = 58.48f, actual = QiblaMath.bearingToQibla(40.7128, -74.0060))
        assertClose(expected = 118.99f, actual = QiblaMath.bearingToQibla(51.5074, -0.1278))
        assertClose(expected = 267.74f, actual = QiblaMath.bearingToQibla(24.8607, 67.0011))
        assertClose(expected = 295.15f, actual = QiblaMath.bearingToQibla(-6.2088, 106.8456))
    }

    @Test
    fun normalizesDegreesIntoCompassRange() {
        assertEquals(0f, QiblaMath.normalizeDegrees(360f))
        assertEquals(315f, QiblaMath.normalizeDegrees(-45f))
        assertEquals(5f, QiblaMath.normalizeDegrees(725f))
    }

    @Test
    fun returnsShortestAngularDistanceAcrossNorth() {
        assertEquals(20f, QiblaMath.shortestAngularDistance(350f, 10f))
        assertEquals(-20f, QiblaMath.shortestAngularDistance(10f, 350f))
        assertEquals(180f, QiblaMath.shortestAngularDistance(0f, 180f))
    }

    @Test
    fun convertsMagneticHeadingToTrueHeading() {
        assertEquals(12f, QiblaMath.magneticToTrueAzimuth(10f, 2f))
        assertEquals(358f, QiblaMath.magneticToTrueAzimuth(2f, -4f))
    }

    @Test
    fun smoothsAcrossZeroWithoutLongRotation() {
        val smoothed = QiblaMath.smoothDegrees(
            previousDegrees = 350f,
            targetDegrees = 10f,
            factor = 0.5f,
        )

        assertEquals(0f, smoothed)
    }

    @Test
    fun validatesConfigBoundaries() {
        assertFailsWith<IllegalArgumentException> {
            QiblaConfig(nearDegrees = 2f, alignedDegrees = 3f)
        }
        assertFailsWith<IllegalArgumentException> {
            QiblaConfig(smoothingFactor = 1.5f)
        }
        assertFailsWith<IllegalArgumentException> {
            QiblaConfig(locationUpdateIntervalMillis = 0L)
        }
    }

    private fun assertClose(expected: Float, actual: Float) {
        assertTrue(
            actual = abs(expected - actual) < 0.05f,
            message = "Expected $actual to be within 0.05 of $expected",
        )
    }
}
