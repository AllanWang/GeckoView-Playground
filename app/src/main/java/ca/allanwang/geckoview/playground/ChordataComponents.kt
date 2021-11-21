package ca.allanwang.geckoview.playground

import android.content.Context
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.state.engine.EngineMiddleware
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.app.links.AppLinksUseCases
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.downloads.DownloadMiddleware
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.prompts.PromptMiddleware
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.thirdparty.com.google.android.exoplayer2.offline.DownloadService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Components for Mozilla
 *
 * Modelled off of Focus:
 * https://github.com/mozilla-mobile/focus-android/blob/main/app/src/main/java/org/mozilla/focus/Components.kt
 */
@Singleton
class ChordataComponents @Inject constructor(
    @ApplicationContext context: Context
) {

    val runtime: GeckoRuntime by lazy {
        logger.atFine().log("Debug %b", BuildConfig.DEBUG)

        val settings = GeckoRuntimeSettings.Builder()
            .consoleOutput(BuildConfig.DEBUG)
            .debugLogging(BuildConfig.DEBUG)
            .javaScriptEnabled(true)
            .build()

        val runtime = GeckoRuntime.create(context, settings)
        runtime.webExtensionController.ensureGeckoTestBuiltIn().accept({ extension ->
            logger.atInfo().log("Extension loaded")
        }, { e ->
            logger.atWarning().withCause(e).log("Extension failed to load")
        })
        runtime
    }

    val engineDefaultSettings: DefaultSettings by lazy {
        DefaultSettings()
    }

    val client: Client by lazy {
        GeckoViewFetchClient(context, runtime)
    }

    val engine: Engine by lazy {
        GeckoEngine(context, engineDefaultSettings, runtime)
    }

    val store: BrowserStore by lazy {
        BrowserStore(
            middleware = listOf(
                DownloadMiddleware(context, DownloadService::class.java),
                PromptMiddleware()
            ) + EngineMiddleware.create(engine)
        )
    }

    val sessionUseCases: SessionUseCases by lazy { SessionUseCases(store) }

    val contextMenuUseCases: ContextMenuUseCases by lazy { ContextMenuUseCases(store) }

    val downloadsUseCases: DownloadsUseCases by lazy { DownloadsUseCases(store) }

    val appLinksUseCases: AppLinksUseCases by lazy { AppLinksUseCases(context) }


    companion object {
        private val logger = FluentLogger.forEnclosingClass()
    }
}