package com.nb.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform