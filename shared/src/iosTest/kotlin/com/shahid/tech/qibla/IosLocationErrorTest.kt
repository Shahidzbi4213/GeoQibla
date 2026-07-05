@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.shahid.tech.qibla

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import platform.CoreLocation.kCLErrorDomain
import platform.CoreLocation.kCLErrorLocationUnknown
import platform.CoreLocation.kCLErrorNetwork
import platform.Foundation.NSError

class IosLocationErrorTest {
    @Test
    fun onlyCoreLocationUnknownErrorsAreTransient() {
        assertTrue(
            NSError(kCLErrorDomain, kCLErrorLocationUnknown, null)
                .isTransientLocationUnknown(),
        )
        assertFalse(
            NSError(kCLErrorDomain, kCLErrorNetwork, null)
                .isTransientLocationUnknown(),
        )
    }
}
