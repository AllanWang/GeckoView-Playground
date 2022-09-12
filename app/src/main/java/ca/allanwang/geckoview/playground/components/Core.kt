package ca.allanwang.geckoview.playground.components

import android.content.Context
import ca.allanwang.geckoview.playground.BuildConfig
import ca.allanwang.geckoview.playground.ChordataActivity
import ca.allanwang.geckoview.playground.R
import ca.allanwang.geckoview.playground.ensureGeckoTestBuiltIn
import com.google.common.flogger.FluentLogger
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.engine.gecko.permission.GeckoSitePermissionsStorage
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.engine.EngineMiddleware
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.permission.SitePermissionsStorage
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.app.links.AppLinksUseCases
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.downloads.AbstractFetchDownloadService
import mozilla.components.feature.downloads.DownloadMiddleware
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.prompts.PromptMiddleware
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.sitepermissions.OnDiskSitePermissionsStorage
import mozilla.components.feature.webnotifications.WebNotificationFeature
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

class Core(private val context: Context) {
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

    val icons by lazy { BrowserIcons(context, client) }

    val store: BrowserStore by lazy {
        BrowserStore(
            middleware = listOf(
//                DownloadMiddleware(context, DownloadService::class.java),
                PromptMiddleware()
            ) + EngineMiddleware.create(engine)
        ).apply {
            icons.install(engine, this)

            WebNotificationFeature(
                context,
                engine,
                icons,
                R.mipmap.ic_launcher_round,
                sitePermissionsStorage,
                ChordataActivity::class.java
            )
        }
    }

    val sitePermissionsStorage: SitePermissionsStorage by lazy {
        GeckoSitePermissionsStorage(runtime, OnDiskSitePermissionsStorage(context))
    }

    val sessionStorage: SessionStorage by lazy {
        SessionStorage(context, engine)
    }

    companion object {
        private val logger = FluentLogger.forEnclosingClass()
    }
}