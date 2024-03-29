package ca.allanwang.geckoview.playground.compose

import android.view.View
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.helper.Target
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView

@Composable
fun Chordata(engine: Engine, store: BrowserStore, target: Target, modifier: Modifier = Modifier) {
  val selectedTab by
    target.observeAsComposableStateFrom(
      store = store,
      observe = { tab ->
        // Render if the tab itself changed or when the state of the linked engine session changes
        arrayOf(
          tab?.id,
          tab?.engineState?.engineSession,
          tab?.engineState?.crashed,
          tab?.content?.firstContentfulPaint,
          // Added on top of Mozilla's WebContent observe values
          tab?.content?.progress,
        )
      }
    )

  //  Box(modifier = modifier) {
  //    ProgressBar(progress = selectedTab?.content?.progress, modifier = Modifier.zIndex(1f))
  WebContent(
    modifier = modifier,
    engine = engine,
    store = store,
    state = WebContentState(selectedTab)
  )
  //  }
}

@Composable
private fun ProgressBar(progress: Int?, modifier: Modifier = Modifier) {
  val shouldDisplay = progress != null && progress in 0 until 100
  LinearProgressIndicator(
    modifier = modifier.alpha(if (shouldDisplay) 1f else 0f).height(2.dp),
    progress = if (progress == null) 0f else progress.toFloat() * 0.01f
  )
}

/** Minimum amount of [SessionState] needed to update [WebContent] */
private data class WebContentState(
  val id: String,
  val engineSession: EngineSession?,
  val canGoBack: Boolean,
) {
  companion object {
    operator fun invoke(tab: SessionState?): WebContentState? {
      tab ?: return null
      return WebContentState(
        id = tab.id,
        engineSession = tab.engineState.engineSession,
        canGoBack = tab.content.canGoBack
      )
    }
  }
}

/**
 * Based on Mozilla WebContent:
 * https://github.com/mozilla-mobile/android-components/blob/main/components/compose/engine/src/main/java/mozilla/components/compose/engine/WebContent.kt
 *
 * WebView from Accompanist:
 * https://github.com/google/accompanist/blob/main/web/src/main/java/com/google/accompanist/web/WebView.kt
 */
@Composable
private fun WebContent(
  modifier: Modifier = Modifier,
  engine: Engine,
  store: BrowserStore,
  state: WebContentState?
) {

  BackHandler(state?.canGoBack == true) {
    val id = state?.id ?: return@BackHandler
    store.dispatch(EngineAction.GoBackAction(id, userInteraction = true))
  }

  AndroidView(
    modifier = modifier.fillMaxSize(),
    factory = { context ->
      val view = engine.createView(context).asView()
//      val parentLayout = NestedScrollView(context)
//      parentLayout.layoutParams =
//        FrameLayout.LayoutParams(
//          FrameLayout.LayoutParams.MATCH_PARENT,
//          FrameLayout.LayoutParams.MATCH_PARENT,
//        )
//      parentLayout.addView(view)
//      parentLayout
      view
    },
    update = { view ->
      var view: View = view
      if (view is NestedScrollView) view = view.getChildAt(0)
      val engineView = view as EngineView
      if (state == null) {
        engineView.release()
      } else {
        val session = state.engineSession
        if (session == null) {
          // This tab does not have an EngineSession that we can render yet. Let's dispatch an
          // action to request creating one. Once one was created and linked to this session, this
          // method will get invoked again.
          store.dispatch(EngineAction.CreateEngineSessionAction(state.id))
        } else {
          engineView.render(session)
        }
      }
    }
  )
}
