package ca.allanwang.geckoview.playground.extension

import com.google.common.flogger.FluentLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mozilla.components.feature.addons.AddonManager

@Singleton
class ChordataAddOnManager @Inject internal constructor(private val addonManager: AddonManager) {

  suspend fun installDarkReader() {
    val addons = withContext(Dispatchers.IO) { addonManager.getAddons() }
    val darkReader = addons.firstOrNull { it.id == DARK_READER_ID }
    if (darkReader == null) {
      logger
        .atInfo()
        .log("Could not find %s in %s", DARK_READER_ID, addons.map { it.id to it.siteUrl })
      return
    }
    if (darkReader.isInstalled()) {
      logger.atInfo().log("DarkReader already installed")
      return
    }
    logger.atInfo().log("Installing DarkReader")
    addonManager.installAddon(
      darkReader,
      onSuccess = { logger.atInfo().log("Installed DarkReader") },
      onError = { msg, t ->
        logger.atSevere().withCause(t).log("Failed to install DarkReader: %s", msg)
      }
    )
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
    private const val DARK_READER_ID = "addon@darkreader.org"
  }
}
