package ca.allanwang.geckoview.playground.features

import com.google.common.flogger.FluentLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.concept.engine.webextension.WebExtensionRuntime
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import mozilla.components.support.webextensions.WebExtensionController
import org.json.JSONObject

class ChordataExtensionFeature(
  private val customTabSessionId: String? = null,
  private val runtime: WebExtensionRuntime,
  private val store: BrowserStore,
) : LifecycleAwareFeature {

  private var extensionController =
    WebExtensionController(
      WEB_CHANNEL_EXTENSION_ID,
      WEB_CHANNEL_EXTENSION_URL,
      WEB_CHANNEL_MESSAGING_ID
    )

  private var scope: CoroutineScope? = null

  override fun start() {
    logger.atInfo().log("start")
    val messageHandler = ChordataBackgroundMessageHandler()
    extensionController.registerBackgroundMessageHandler(
      messageHandler,
      WEB_CHANNEL_BACKGROUND_MESSAGING_ID
    )

    extensionController.install(runtime)

    scope =
      store.flowScoped { flow ->
        flow
          .mapNotNull { state -> state.findCustomTabOrSelectedTab(customTabSessionId) }
          .ifChanged { it.engineState.engineSession }
          .collect {
            it.engineState.engineSession?.let { engineSession ->
              registerContentMessageHandler(engineSession)
            }
          }
      }
  }

  override fun stop() {
    logger.atInfo().log("stop")
    scope?.cancel()
  }

  private fun registerContentMessageHandler(engineSession: EngineSession) {
    val messageHandler = ChordataMessageHandler()
    extensionController.registerContentMessageHandler(engineSession, messageHandler)
  }

  private class ChordataBackgroundMessageHandler : MessageHandler {
    override fun onPortConnected(port: Port) {
      logger.atInfo().log("hello message handler connected %s", port.name())
      port.postMessage(JSONObject().put("text", "hello world; background port connected"))
    }
  }

  private class ChordataMessageHandler : MessageHandler {
    override fun onMessage(message: Any, source: EngineSession?): Any? {
      logger.atFine().log("onMessage: %s", message)
      return null
    }

    override fun onPortMessage(message: Any, port: Port) {
      logger.atFine().log("onPortMessage: %s", message)
    }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()

    const val WEB_CHANNEL_EXTENSION_ID = "geckoview_chordata_test@pitchedapps"
    const val WEB_CHANNEL_MESSAGING_ID = "chordataTestChannel"
    const val WEB_CHANNEL_BACKGROUND_MESSAGING_ID = "chordataTestChannelBackground"
    const val WEB_CHANNEL_EXTENSION_URL = "resource://android/assets/geckotest/"
  }
}
