package ca.allanwang.geckoview.playground

import android.content.Context
import ca.allanwang.geckoview.playground.components.Core
import ca.allanwang.geckoview.playground.components.UseCases
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Components for Mozilla
 *
 * Modelled off of Focus:
 * https://github.com/mozilla-mobile/focus-android/blob/main/app/src/main/java/org/mozilla/focus/Components.kt
 */
@Singleton
class ChordataComponents @Inject constructor(@ApplicationContext context: Context) {

  val core: Core by lazy { Core(context) }

  val useCases: UseCases by lazy { UseCases(context, core) }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
