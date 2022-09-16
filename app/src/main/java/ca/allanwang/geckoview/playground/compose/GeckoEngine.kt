package ca.allanwang.geckoview.playground.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ca.allanwang.geckoview.playground.chordata.ChordataSession
import org.mozilla.geckoview.GeckoView

@Composable
fun GeckoEngine(session: ChordataSession, initSession: (ChordataSession) -> Unit) {
  AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context -> GeckoView(context) },
      update = { view ->
        when {
          view.session == null -> {
            initSession(session)
            view.setSession(session)
          }
          view.session != session -> {
            if (view.session != null) {
              view.releaseSession()
            }
            view.setSession(session)
          }
        }
      })
}
