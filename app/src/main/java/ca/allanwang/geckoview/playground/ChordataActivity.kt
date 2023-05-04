package ca.allanwang.geckoview.playground

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
import dagger.hilt.android.AndroidEntryPoint
import mozilla.components.concept.engine.EngineView
import javax.inject.Inject

@AndroidEntryPoint
class ChordataActivity : AppCompatActivity() {

  @Inject internal lateinit var components: ChordataComponents

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportFragmentManager
        .beginTransaction()
        .replace(android.R.id.content, ChordataFragment())
        .commit()
  }

  override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
    return if (name == EngineView::class.java.name)
        components.core.engine.createView(context, attrs).asView()
    else super.onCreateView(name, context, attrs)
  }

}
