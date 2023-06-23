package ca.allanwang.geckoview.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import ca.allanwang.geckoview.playground.compose.ChordataScreen2
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChordataComposeActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    logger.atInfo().log("Compose start activity")
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent { MaterialTheme { ChordataScreen2(modifier = Modifier.systemBarsPadding()) } }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
