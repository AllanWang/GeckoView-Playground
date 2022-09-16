package ca.allanwang.geckoview.playground.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GeckoActivity : AppCompatActivity() {

  @Inject internal lateinit var components: ChordataComponents

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { MaterialTheme { GeckoScreen(components) } }
  }
}
