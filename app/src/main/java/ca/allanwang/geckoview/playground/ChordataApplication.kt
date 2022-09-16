package ca.allanwang.geckoview.playground

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.webextensions.WebExtensionSupport

@HiltAndroidApp
class ChordataApplication : Application() {

  // Engine needs to be created lazily in the application
  @Inject internal lateinit var engineProvider: Provider<Engine>
  @Inject internal lateinit var storeProvider: Provider<BrowserStore>
  @Inject internal lateinit var sessionStorageProvider: Provider<SessionStorage>
  @Inject internal lateinit var tabsUseCasesProvider: Provider<TabsUseCases>

  private val engine
    get() = engineProvider.get()
  private val store
    get() = storeProvider.get()
  private val sessionStorage
    get() = sessionStorageProvider.get()
  private val tabsUseCases
    get() = tabsUseCasesProvider.get()

  override fun onCreate() {
    super.onCreate()

    setupLogging()

    // Do not run for child processes spawned by Gecko
    if (!isMainProcess()) return

    engine.warmUp()

//    restoreBrowserState()

    WebExtensionSupport.initialize(
      runtime = engine,
      store = store,
//      onNewTabOverride = { _, engineSession, url ->
//        val tabId = tabsUseCases.addTab(url = url, selectTab = true, engineSession = engineSession)
//        tabId
//      },
//      onCloseTabOverride = { _, sessionId -> tabsUseCases.removeTab(sessionId) },
//      onSelectTabOverride = { _, sessionId -> tabsUseCases.selectTab(sessionId) },
    )
  }

  private fun setupLogging() {
    Log.addSink(AndroidLogSink())
  }

  private fun restoreBrowserState() =
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
}
