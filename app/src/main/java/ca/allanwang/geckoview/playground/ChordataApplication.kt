package ca.allanwang.geckoview.playground

import androidx.multidex.MultiDexApplication
import ca.allanwang.geckoview.playground.extension.ChordataAddOnManager
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.update.AddonUpdater
import mozilla.components.feature.addons.update.GlobalAddonDependencyProvider
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.webextensions.WebExtensionSupport

@HiltAndroidApp
class ChordataApplication : MultiDexApplication() {

  @Inject internal lateinit var applicationHilt: Provider<ApplicationHilt>

  /** Application injections need to be added lazily */
  @Singleton
  class ApplicationHilt
  @Inject
  internal constructor(
      val engine: Engine,
      val store: BrowserStore,
      val sessionStorage: SessionStorage,
      val tabsUseCases: TabsUseCases,
      val addonUpdater: AddonUpdater,
      val addonManager: AddonManager,
      val chordataAddOnManager: ChordataAddOnManager,
  )

  override fun onCreate() {
    super.onCreate()

    setupLogging()

    // Do not run for child processes spawned by Gecko
    if (!isMainProcess()) return

    applicationHilt.get().init()
  }

  private fun ApplicationHilt.init() {
    engine.warmUp()

    //    restoreBrowserState()

    try {
      GlobalAddonDependencyProvider.initialize(
          addonManager,
          addonUpdater,
          onCrash = { logger.atSevere().withCause(it).log("GlobalAddonDependencyProvider crash") })
      WebExtensionSupport.initialize(
          runtime = engine,
          store = store,
          //      onNewTabOverride = { _, engineSession, url ->
          //        val tabId = tabsUseCases.addTab(url = url, selectTab = true, engineSession =
          // engineSession)
          //        tabId
          //      },
          //      onCloseTabOverride = { _, sessionId -> tabsUseCases.removeTab(sessionId) },
          //      onSelectTabOverride = { _, sessionId -> tabsUseCases.selectTab(sessionId) },
          )

      //      installDefaultAddons()
    } catch (e: UnsupportedOperationException) {
      // Web extension support is only available for engine gecko
      logger.atSevere().withCause(e).log("Failed to initialize web extension support")
    }
  }

  private fun setupLogging() {
    Log.addSink(AndroidLogSink())
  }

  @OptIn(DelicateCoroutinesApi::class)
  private fun ApplicationHilt.restoreBrowserState() =
      GlobalScope.launch(Dispatchers.Main) {
        tabsUseCases.restore(sessionStorage)

        // Now that we have restored our previous state (if there's one) let's setup auto saving the
        // state while
        // the app is used.
        sessionStorage
            .autoSave(store)
            .periodicallyInForeground(interval = 30, unit = TimeUnit.SECONDS)
            .whenGoingToBackground()
            .whenSessionsChange()
      }

  @OptIn(DelicateCoroutinesApi::class)
  private fun ApplicationHilt.installDefaultAddons() =
      GlobalScope.launch(Dispatchers.Main) { chordataAddOnManager.installDarkReader() }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
