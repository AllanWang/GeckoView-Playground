package ca.allanwang.geckoview.playground

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import ca.allanwang.geckoview.playground.compose.ChordataScreen
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChordataComposeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    logger.atInfo().log("Compose start activity")

    setContent { MaterialTheme { ChordataScreen() } }
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
