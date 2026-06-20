package com.shahid.tech.qibla

/**
 * Runtime thresholds and callbacks used by the default GeoQibla controller.
 *
 * All degree values are expressed in clockwise compass degrees. Safe defaults
 * mirror the Android reference behavior while allowing callers to tune the UI
 * without replacing the controller.
 */
data class QiblaConfig(
    val nearDegrees: Float = 10f,
    val alignedDegrees: Float = 3f,
    val stableAlignedDurationMillis: Long = 750L,
    val smoothingFactor: Float = 0.15f,
    val locationUpdateIntervalMillis: Long = 1_000L,
    val tiltLimitDegrees: Float = 55f,
    val magneticFieldMinMicrotesla: Float = 25f,
    val magneticFieldMaxMicrotesla: Float = 65f,
    val hapticsEnabled: Boolean = true,
    val onAligned: (() -> Unit)? = null,
) {
    init {
        require(nearDegrees >= alignedDegrees) {
            "nearDegrees must be greater than or equal to alignedDegrees"
        }
        require(alignedDegrees >= 0f) { "alignedDegrees must be non-negative" }
        require(stableAlignedDurationMillis >= 0L) {
            "stableAlignedDurationMillis must be non-negative"
        }
        require(smoothingFactor in 0f..1f) { "smoothingFactor must be between 0 and 1" }
        require(locationUpdateIntervalMillis > 0L) {
            "locationUpdateIntervalMillis must be positive"
        }
        require(tiltLimitDegrees in 0f..90f) { "tiltLimitDegrees must be between 0 and 90" }
        require(magneticFieldMinMicrotesla <= magneticFieldMaxMicrotesla) {
            "magneticFieldMinMicrotesla must be <= magneticFieldMaxMicrotesla"
        }
    }
}
