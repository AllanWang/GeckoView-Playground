package ca.allanwang.geckoview.playground

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import ca.allanwang.geckoview.playground.compose.ChordataScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChordataComposeActivity : AppCompatActivity() {

  @Inject internal lateinit var components: ChordataComponents

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { MaterialTheme { ChordataScreen(components) } }
  }
}
