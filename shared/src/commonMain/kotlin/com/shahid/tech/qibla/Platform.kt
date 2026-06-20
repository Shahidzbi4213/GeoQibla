package com.shahid.tech.qibla

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform