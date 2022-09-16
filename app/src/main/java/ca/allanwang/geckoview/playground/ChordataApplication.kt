package ca.allanwang.geckoview.playground

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.webextensions.WebExtensionSupport
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ChordataApplication : Application() {

  @Inject internal lateinit var components: ChordataComponents

  override fun onCreate() {
    super.onCreate()

    setupLogging()

    // Do not run for child processes spawned by Gecko
    if (!isMainProcess()) return

    components.core.engine.warmUp()

    restoreBrowserState()

    WebExtensionSupport.initialize(
        runtime = components.core.engine,
        store = components.core.store,
        onNewTabOverride = { _, engineSession, url ->
          val tabId =
              components.useCases.tabsUseCases.addTab(
                  url = url, selectTab = true, engineSession = engineSession)
          tabId
        },
        onCloseTabOverride = { _, sessionId ->
          components.useCases.tabsUseCases.removeTab(sessionId)
        },
        onSelectTabOverride = { _, sessionId ->
          components.useCases.tabsUseCases.selectTab(sessionId)
        },
    )
  }

  private fun setupLogging() {
    Log.addSink(AndroidLogSink())
  }

  private fun restoreBrowserState() =
      GlobalScope.launch(Dispatchers.Main) {
        val store = components.core.store
        val sessionStorage = components.core.sessionStorage

        components.useCases.tabsUseCases.restore(sessionStorage)

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
