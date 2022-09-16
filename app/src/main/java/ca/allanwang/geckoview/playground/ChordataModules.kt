package ca.allanwang.geckoview.playground

import com.google.common.flogger.FluentLogger
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mozilla.geckoview.WebExtensionController

@Module
@InstallIn(SingletonComponent::class)
object ChordataModules {

  private val logger = FluentLogger.forEnclosingClass()
}

fun WebExtensionController.ensureGeckoTestBuiltIn() =
    ensureBuiltIn("resource://android/assets/geckotest/", "geckoview_chordata_test@pitchedapps")
