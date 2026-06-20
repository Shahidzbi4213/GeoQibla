package com.shahid.tech.qibla

import androidx.compose.ui.unit.LayoutDirection

data class QiblaStrings(
    val layoutDirection: LayoutDirection = LayoutDirection.Ltr,
    val title: String = "Qibla",
    val subtitle: String = "Face the direction of the Kaaba",
    val idleTitle: String = "Ready when you are",
    val idleMessage: String = "Start the compass to calculate Qibla direction.",
    val permissionTitle: String = "Location permission needed",
    val permissionMessage: String = "GeoQibla uses your location to calculate the Qibla bearing.",
    val permissionDeniedTitle: String = "Location permission denied",
    val permissionDeniedMessage: String = "Allow location access to continue.",
    val permissionPermanentlyDeniedTitle: String = "Enable permission in settings",
    val permissionPermanentlyDeniedMessage: String = "Location access is disabled for this app.",
    val locationDisabledTitle: String = "Location is off",
    val locationDisabledMessage: String = "Turn on device location services to continue.",
    val locatingTitle: String = "Finding your location",
    val locatingMessage: String = "Waiting for a location fix.",
    val sensorUnavailableTitle: String = "Compass unavailable",
    val sensorUnavailableMessage: String = "This device cannot provide heading updates.",
    val calibrationTitle: String = "Compass needs calibration",
    val calibrationMessage: String = "Move the device in a figure-eight motion until accuracy improves.",
    val tiltedTitle: String = "Hold the device level",
    val tiltedMessage: String = "Flatten the device for a more accurate compass reading.",
    val readyTitle: String = "Turn toward Qibla",
    val readyMessage: String = "Follow the arrow until it points straight ahead.",
    val nearTitle: String = "Almost aligned",
    val nearMessage: String = "Make a small adjustment to face Qibla.",
    val alignedTitle: String = "Aligned",
    val alignedMessage: String = "You are facing Qibla.",
    val errorTitle: String = "Unable to calculate direction",
    val requestPermissionAction: String = "Allow location",
    val openSettingsAction: String = "Open settings",
    val openLocationSettingsAction: String = "Location settings",
    val retryAction: String = "Retry",
    val dismissAction: String = "Dismiss",
    val qiblaBearingLabel: String = "Qibla bearing",
    val currentHeadingLabel: String = "Current heading",
    val directionDeltaLabel: String = "Adjustment",
    val distanceLabel: String = "Distance",
    val locationLabel: String = "Location",
    val accuracyLabel: String = "Sensor accuracy",
    val sourceLabel: String = "Heading source",
    val leftValue: String = "left",
    val rightValue: String = "right",
    val straightValue: String = "straight",
    val compassContentDescription: String = "Qibla compass dial",
    val unavailableValue: String = "Unavailable",
) {
    companion object {
        fun default(): QiblaStrings = english()

        fun english(): QiblaStrings = QiblaStrings()

        fun arabic(): QiblaStrings = QiblaStrings(
            layoutDirection = LayoutDirection.Rtl,
            title = "القبلة",
            subtitle = "اتجه نحو الكعبة المشرفة",
            idleTitle = "جاهز عند البدء",
            idleMessage = "ابدأ البوصلة لحساب اتجاه القبلة.",
            permissionTitle = "إذن الموقع مطلوب",
            permissionMessage = "يستخدم GeoQibla موقعك لحساب اتجاه القبلة.",
            permissionDeniedTitle = "تم رفض إذن الموقع",
            permissionDeniedMessage = "اسمح بالوصول إلى الموقع للمتابعة.",
            permissionPermanentlyDeniedTitle = "فعّل الإذن من الإعدادات",
            permissionPermanentlyDeniedMessage = "الوصول إلى الموقع معطل لهذا التطبيق.",
            locationDisabledTitle = "الموقع متوقف",
            locationDisabledMessage = "فعّل خدمات الموقع على الجهاز للمتابعة.",
            locatingTitle = "جار تحديد موقعك",
            locatingMessage = "بانتظار قراءة الموقع.",
            sensorUnavailableTitle = "البوصلة غير متاحة",
            sensorUnavailableMessage = "لا يمكن لهذا الجهاز توفير تحديثات الاتجاه.",
            calibrationTitle = "البوصلة تحتاج إلى معايرة",
            calibrationMessage = "حرّك الجهاز على شكل رقم ثمانية حتى تتحسن الدقة.",
            tiltedTitle = "أمسك الجهاز بشكل مستو",
            tiltedMessage = "اجعل الجهاز مستويا للحصول على قراءة أدق.",
            readyTitle = "اتجه نحو القبلة",
            readyMessage = "اتبع السهم حتى يشير إلى الأمام مباشرة.",
            nearTitle = "قريب من الاتجاه",
            nearMessage = "أجر تعديلا بسيطا لمواجهة القبلة.",
            alignedTitle = "تمت المحاذاة",
            alignedMessage = "أنت الآن باتجاه القبلة.",
            errorTitle = "تعذر حساب الاتجاه",
            requestPermissionAction = "السماح بالموقع",
            openSettingsAction = "فتح الإعدادات",
            openLocationSettingsAction = "إعدادات الموقع",
            retryAction = "إعادة المحاولة",
            dismissAction = "إخفاء",
            qiblaBearingLabel = "اتجاه القبلة",
            currentHeadingLabel = "الاتجاه الحالي",
            directionDeltaLabel = "التعديل",
            distanceLabel = "المسافة",
            locationLabel = "الموقع",
            accuracyLabel = "دقة المستشعر",
            sourceLabel = "مصدر الاتجاه",
            leftValue = "يسارا",
            rightValue = "يمينا",
            straightValue = "مباشرة",
            compassContentDescription = "قرص بوصلة القبلة",
            unavailableValue = "غير متاح",
        )
    }
}

internal data class QiblaMessage(
    val title: String,
    val body: String,
)

internal fun QiblaStrings.messageFor(state: QiblaState): QiblaMessage =
    when (state.uiState) {
        QiblaUiState.IDLE -> QiblaMessage(idleTitle, idleMessage)
        QiblaUiState.REQUESTING_PERMISSION,
        QiblaUiState.PERMISSION_REQUIRED,
        -> QiblaMessage(permissionTitle, permissionMessage)

        QiblaUiState.PERMISSION_DENIED -> QiblaMessage(
            permissionDeniedTitle,
            permissionDeniedMessage,
        )

        QiblaUiState.PERMISSION_PERMANENTLY_DENIED -> QiblaMessage(
            permissionPermanentlyDeniedTitle,
            permissionPermanentlyDeniedMessage,
        )

        QiblaUiState.LOCATION_DISABLED -> QiblaMessage(
            locationDisabledTitle,
            locationDisabledMessage,
        )

        QiblaUiState.LOCATING -> QiblaMessage(locatingTitle, locatingMessage)
        QiblaUiState.SENSOR_UNAVAILABLE -> QiblaMessage(
            sensorUnavailableTitle,
            sensorUnavailableMessage,
        )

        QiblaUiState.CALIBRATION_NEEDED -> QiblaMessage(calibrationTitle, calibrationMessage)
        QiblaUiState.TILTED -> QiblaMessage(tiltedTitle, tiltedMessage)
        QiblaUiState.READY -> QiblaMessage(readyTitle, readyMessage)
        QiblaUiState.NEAR_QIBLA -> QiblaMessage(nearTitle, nearMessage)
        QiblaUiState.ALIGNED -> QiblaMessage(alignedTitle, alignedMessage)
        QiblaUiState.ERROR -> QiblaMessage(
            errorTitle,
            state.errorMessage ?: errorTitle,
        )
    }
