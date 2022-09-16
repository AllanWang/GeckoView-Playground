package ca.allanwang.geckoview.playground.hilt

import ca.allanwang.geckoview.playground.components.Core
import ca.allanwang.geckoview.playground.components.UseCases
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Components for Mozilla
 *
 * Modelled off of Focus:
 * https://github.com/mozilla-mobile/focus-android/blob/main/app/src/main/java/org/mozilla/focus/Components.kt
 * but with hilt
 */
@Singleton
class ChordataComponents @Inject internal constructor(val core: Core, val useCases: UseCases)
