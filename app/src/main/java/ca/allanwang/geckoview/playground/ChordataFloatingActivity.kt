package ca.allanwang.geckoview.playground

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import ca.allanwang.geckoview.playground.ChordataFloatingActivity.Companion.EXTRA_URL
import ca.allanwang.geckoview.playground.compose.ChordataFloatingViewModel
import ca.allanwang.geckoview.playground.compose.FloatingScreen
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChordataFloatingActivity : ComponentActivity() {

  private val viewModel: ChordataFloatingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logger.atInfo().log("Compose start floating activity")
    WindowCompat.setDecorFitsSystemWindows(window, false)
    viewModel.baseUrl = intent.getStringExtra(EXTRA_URL) ?: return finish()
    setContent { MaterialTheme { FloatingScreen(modifier = Modifier.systemBarsPadding()) } }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    this.intent = intent
    viewModel.baseUrl = intent.getStringExtra(EXTRA_URL) ?: return finish()
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()

    internal const val EXTRA_URL = "extra_url"
  }
}

fun Context.launchFloatingUrl(url: String) {
  val intent = Intent(this, ChordataFloatingActivity::class.java)
  intent.putExtra(EXTRA_URL, url)
  startActivity(intent)
}
