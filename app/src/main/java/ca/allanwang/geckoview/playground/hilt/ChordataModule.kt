package ca.allanwang.geckoview.playground.hilt

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import ca.allanwang.geckoview.playground.BuildConfig
import ca.allanwang.geckoview.playground.ChordataActivity
import ca.allanwang.geckoview.playground.R
import ca.allanwang.geckoview.playground.components.usecases.HomeTabsUseCases
import com.google.common.flogger.FluentLogger
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.engine.gecko.permission.GeckoSitePermissionsStorage
import mozilla.components.browser.engine.system.SystemEngine
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

@Qualifier annotation class Chordata

@Module
@InstallIn(SingletonComponent::class)
interface ChordataBindModule {
  @BindsOptionalOf @Chordata fun userAgent(): String
}

@Module
@InstallIn(SingletonComponent::class)
object ChordataModule {

  private val logger = FluentLogger.forEnclosingClass()

  /**
   * Windows based user agent.
   *
   * Note that Facebook's mobile webpage for mobile user agents is completely different from the
   * desktop ones. All elements become divs, so nothing can be queried. There is a new UI too, but
   * it doesn't seem worth migrating all other logic over.
   */
  private const val USER_AGENT_WINDOWS =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/112.0"

  private const val USER_AGENT_WINDOWS_FROST =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.90 Safari/537.36"
  @Provides @Singleton @Chordata fun userAgent(): String = USER_AGENT_WINDOWS_FROST

  @Provides
  @Singleton
  fun geckoRuntime(@ApplicationContext context: Context): GeckoRuntime {
    val settings =
      GeckoRuntimeSettings.Builder()
        .consoleOutput(BuildConfig.DEBUG)
        .loginAutofillEnabled(true)
        //        .debugLogging(false)
        .debugLogging(BuildConfig.DEBUG)
        .javaScriptEnabled(true)
        .build()

    return GeckoRuntime.create(context, settings)
  }

  @Provides
  @Singleton
  fun client(@ApplicationContext context: Context, runtime: GeckoRuntime): Client {
    return GeckoViewFetchClient(context, runtime)
  }

  @Provides
  @Singleton
  fun settings(@Chordata userAgent: Optional<String>): Settings {
    return DefaultSettings(userAgentString = userAgent.getOrNull())
  }

  @Provides
  @Singleton
  fun engine(geckoEngine: GeckoEngine, systemEngine: SystemEngine):Engine {
    return geckoEngine
  }

  @Provides
  @Singleton
  fun systemEngine(@ApplicationContext context: Context) : SystemEngine {
    return SystemEngine(context)
  }

  @Provides
  @Singleton
  fun geckoEngine(
    @ApplicationContext context: Context,
    settings: Settings,
    runtime: GeckoRuntime
  ): GeckoEngine {
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
        logger.atInfo().log("BrowserAction: LoadUrlAction %s", action)
      } else {
        logger.atFine().log("BrowserAction: %s", action)
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
      notificationsDelegate = notificationsDelegate
    )
    return store
  }

  @Provides
  @Singleton
  fun notificationDelegate(@ApplicationContext context: Context): NotificationsDelegate {
    return NotificationsDelegate(NotificationManagerCompat.from(context))
  }
}
