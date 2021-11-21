package ca.allanwang.geckoview.playground

import android.content.Context
import com.google.common.flogger.FluentLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.WebExtensionController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChordataModules {

    private val logger = FluentLogger.forEnclosingClass()

    @Provides
    @Singleton
    fun runtime(@ApplicationContext appContext: Context): GeckoRuntime {
        logger.atFine().log("Debug %b", BuildConfig.DEBUG)

        val settings = GeckoRuntimeSettings.Builder()
            .consoleOutput(BuildConfig.DEBUG)
            .debugLogging(BuildConfig.DEBUG)
            .javaScriptEnabled(true)
            .build()

        val runtime = GeckoRuntime.create(appContext, settings)
        runtime.webExtensionController.ensureGeckoTestBuiltIn().accept({ extension ->
            logger.atInfo().log("Extension loaded")
        }, { e ->
            logger.atWarning().withCause(e).log("Extension failed to load")
        })
        return runtime
    }

}

fun WebExtensionController.ensureGeckoTestBuiltIn() = ensureBuiltIn(
    "resource://android/assets/geckotest/",
    "geckoview_chordata_test@pitchedapps"
)