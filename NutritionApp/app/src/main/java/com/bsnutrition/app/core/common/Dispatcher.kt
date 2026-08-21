package com.bsnutrition.app.core.common

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val bsnDispatcher: BsnDispatchers)

enum class BsnDispatchers {
    Default,
    IO,
    Main
}
