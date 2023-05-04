package ca.allanwang.geckoview.playground.hilt

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import ca.allanwang.geckoview.playground.BuildConfig
import ca.allanwang.geckoview.playground.ChordataActivity
import ca.allanwang.geckoview.playground.R
import ca.allanwang.geckoview.playground.components.usecases.HomeTabsUseCases
import com.google.common.flogger.FluentLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.engine.gecko.permission.GeckoSitePermissionsStorage
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.engine.EngineMiddleware
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.Settings
import mozilla.components.concept.engine.permission.SitePermissionsStorage
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.prompts.PromptMiddleware
import mozilla.components.feature.sitepermissions.OnDiskSitePermissionsStorage
import mozilla.components.feature.webnotifications.WebNotificationFeature
import mozilla.components.lib.state.Middleware
import mozilla.components.lib.state.MiddlewareContext
import mozilla.components.support.base.android.NotificationsDelegate
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.WebExtensionController

@Module
@InstallIn(SingletonComponent::class)
object ChordataModule {

  private val logger = FluentLogger.forEnclosingClass()

  @Provides
  @Singleton
  fun geckoRuntime(@ApplicationContext context: Context): GeckoRuntime {
    val settings =
        GeckoRuntimeSettings.Builder()
            .consoleOutput(BuildConfig.DEBUG)
            .debugLogging(BuildConfig.DEBUG)
            .javaScriptEnabled(true)
            .build()

    val runtime = GeckoRuntime.create(context, settings)
    runtime.webExtensionController
        .ensureGeckoTestBuiltIn()
        .accept(
            { logger.atInfo().log("Extension loaded") },
            { e -> logger.atWarning().withCause(e).log("Extension failed to load") })
    return runtime
  }

  @Provides
  @Singleton
  fun client(@ApplicationContext context: Context, runtime: GeckoRuntime): Client {
    return GeckoViewFetchClient(context, runtime)
  }

  @Provides
  @Singleton
  fun settings(): Settings {
    return DefaultSettings()
  }

  @Provides
  @Singleton
  fun engine(
      @ApplicationContext context: Context,
      settings: Settings,
      runtime: GeckoRuntime
  ): Engine {
    return GeckoEngine(context, settings, runtime)
  }

  @Provides
  @Singleton
  fun browserIcons(@ApplicationContext context: Context, client: Client): BrowserIcons {
    return BrowserIcons(context, client)
  }

  @Provides
  @Singleton
  fun sitePermissionStorage(
      @ApplicationContext context: Context,
      runtime: GeckoRuntime
  ): SitePermissionsStorage {
    return GeckoSitePermissionsStorage(runtime, OnDiskSitePermissionsStorage(context))
  }

  @Provides
  @Singleton
  fun sessionStorage(@ApplicationContext context: Context, engine: Engine): SessionStorage {
    return SessionStorage(context, engine)
  }

  private class LoggerMiddleWare : Middleware<BrowserState, BrowserAction> {
    override fun invoke(
        context: MiddlewareContext<BrowserState, BrowserAction>,
        next: (BrowserAction) -> Unit,
        action: BrowserAction
    ) {
      if (action is EngineAction.LoadUrlAction) {
        logger.atInfo().log("BrowserAction: LoadUrlAction %s", action.url)
      } else {
        logger.atInfo().log("BrowserAction: %s", action::class.simpleName)
      }
      next(action)
    }
  }

  @Provides
  @Singleton
  fun browserStore(
      @ApplicationContext context: Context,
      icons: BrowserIcons,
      sitePermissionsStorage: SitePermissionsStorage,
      engine: Engine,
      notificationsDelegate: NotificationsDelegate,
  ): BrowserStore {

    val middleware = buildList {
      if (BuildConfig.DEBUG) add(LoggerMiddleWare())
      add(HomeTabsUseCases.HomeMiddleware())
      add(PromptMiddleware())
      //      add(DownloadMiddleware(context, DownloadService::class.java))
      addAll(EngineMiddleware.create(engine))
    }

    val store = BrowserStore(middleware = middleware)
    icons.install(engine, store)
    WebNotificationFeature(
        context = context,
        engine = engine,
        browserIcons = icons,
        smallIcon = R.mipmap.ic_launcher_round,
        sitePermissionsStorage = sitePermissionsStorage,
        activityClass = ChordataActivity::class.java,
        notificationsDelegate = notificationsDelegate)
    return store
  }

  @Provides
  @Singleton
  fun notificationDelegate(@ApplicationContext context: Context): NotificationsDelegate {
    return NotificationsDelegate(NotificationManagerCompat.from(context))
  }

  private fun WebExtensionController.ensureGeckoTestBuiltIn() =
      ensureBuiltIn("resource://android/assets/geckotest/", "geckoview_chordata_test@pitchedapps")
}
