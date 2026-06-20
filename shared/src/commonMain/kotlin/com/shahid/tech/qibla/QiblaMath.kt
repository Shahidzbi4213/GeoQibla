package com.shahid.tech.qibla

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal object QiblaMath {
    private const val KaabaLatitude = 21.422487
    private const val KaabaLongitude = 39.826206
    private const val EarthRadiusMeters = 6_371_000.0

    fun bearingToQibla(latitude: Double, longitude: Double): Float {
        val fromLatitude = latitude.toRadians()
        val deltaLongitude = (KaabaLongitude - longitude).toRadians()
        val kaabaLatitude = KaabaLatitude.toRadians()

        val y = sin(deltaLongitude)
        val x = cos(fromLatitude) * tan(kaabaLatitude) -
            sin(fromLatitude) * cos(deltaLongitude)

        return normalizeDegrees(atan2(y, x).toDegrees().toFloat())
    }

    fun distanceToKaabaMeters(latitude: Double, longitude: Double): Double {
        val fromLatitude = latitude.toRadians()
        val kaabaLatitude = KaabaLatitude.toRadians()
        val deltaLatitude = (KaabaLatitude - latitude).toRadians()
        val deltaLongitude = (KaabaLongitude - longitude).toRadians()

        val a = sin(deltaLatitude / 2).pow(2) +
            cos(fromLatitude) * cos(kaabaLatitude) * sin(deltaLongitude / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EarthRadiusMeters * c
    }

    fun normalizeDegrees(value: Float): Float {
        val normalized = value % 360f
        return if (normalized < 0f) normalized + 360f else normalized
    }

    fun shortestAngularDistance(fromDegrees: Float, toDegrees: Float): Float {
        val delta = normalizeDegrees(toDegrees) - normalizeDegrees(fromDegrees)
        return when {
            delta > 180f -> delta - 360f
            delta < -180f -> delta + 360f
            else -> delta
        }
    }

    fun magneticToTrueAzimuth(
        magneticAzimuthDegrees: Float,
        declinationDegrees: Float,
    ): Float = normalizeDegrees(magneticAzimuthDegrees + declinationDegrees)

    fun smoothDegrees(previousDegrees: Float?, targetDegrees: Float, factor: Float): Float {
        if (previousDegrees == null || factor >= 1f) return normalizeDegrees(targetDegrees)
        if (factor <= 0f) return normalizeDegrees(previousDegrees)

        val delta = shortestAngularDistance(previousDegrees, targetDegrees)
        return normalizeDegrees(previousDegrees + delta * factor)
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
    private fun Double.toDegrees(): Double = this * 180.0 / PI
    private fun tan(value: Double): Double = sin(value) / cos(value)
}
