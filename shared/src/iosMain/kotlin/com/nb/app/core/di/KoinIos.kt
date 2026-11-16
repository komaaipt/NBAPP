package com.nb.app.core.di

/**
 * Swift-friendly Koin entry point, called from `iOSApp.init()` as `KoinIosKt.doInitKoinIos()`.
 * Kotlin default arguments are not exported to Swift, so [initKoin] can't be called directly.
 */
fun initKoinIos() {
    initKoin()
}
