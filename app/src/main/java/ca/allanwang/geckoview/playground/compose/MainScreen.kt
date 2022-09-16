package ca.allanwang.geckoview.playground.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.allanwang.geckoview.playground.ChordataFragment
import ca.allanwang.geckoview.playground.components.UseCases
import mozilla.components.browser.state.helper.Target
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.compose.engine.WebContent
import mozilla.components.concept.engine.Engine

@Composable
fun MainScreen(
  engine: Engine,
  store: BrowserStore,
  contextId: String,
  useCases: UseCases,
  tabIndex: Int,
  onTabSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {

  LaunchedEffect(contextId) {
    useCases.homeTabs.createHomeTabs(
      contextId,
      tabIndex,
      listOf(ChordataFragment.FACEBOOK_M_URL, ChordataFragment.GITHUB_URL)
    )
  }

  Column(modifier = modifier) {
    TabRow(selectedTabIndex = tabIndex, indicator = {}) {
      listOf("A", "B").forEachIndexed { i, text ->
        val selected = tabIndex == i
        Tab(
          selected = selected,
          onClick = { onTabSelect(i) }
        ) {
          Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
          )
        }
      }
    }

    // For tab switching, must use SelectedTab
    // https://github.com/mozilla-mobile/android-components/issues/12798
    WebContent(engine = engine, store = store, target = Target.SelectedTab)
  }
}
