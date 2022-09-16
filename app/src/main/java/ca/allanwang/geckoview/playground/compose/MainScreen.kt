package ca.allanwang.geckoview.playground.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.FACEBOOK_M_URL
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.GITHUB_URL
import ca.allanwang.geckoview.playground.components.UseCases
import mozilla.components.browser.state.helper.Target
import mozilla.components.browser.state.store.BrowserStore
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

  val tabItems = remember {
    listOf(
      MainTabItem(title = "Title1", icon = Icons.Default.Add, url = FACEBOOK_M_URL),
      MainTabItem(title = "Title2", icon = Icons.Default.BugReport, url = GITHUB_URL),
    )
  }

  LaunchedEffect(contextId) {
    useCases.homeTabs.createHomeTabs(contextId, tabIndex, tabItems.map { it.url })
  }

  Column(modifier = modifier) {
    MainTabRow(selectedIndex = tabIndex, items = tabItems, onTabSelect = onTabSelect)
    // For tab switching, must use SelectedTab
    // https://github.com/mozilla-mobile/android-components/issues/12798
    Chordata(engine = engine, store = store, target = Target.SelectedTab)
  }
}

data class MainTabItem(val title: String, val icon: ImageVector, val url: String)

@Composable
fun MainTabRow(
  selectedIndex: Int,
  items: List<MainTabItem>,
  onTabSelect: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  TabRow(modifier = modifier, selectedTabIndex = selectedIndex, indicator = {}) {
    items.forEachIndexed { i, item ->
      val selected = selectedIndex == i
      Tab(selected = selected, onClick = { onTabSelect(i) }) {
        MainTabItem(modifier = Modifier.padding(16.dp), item = item, selected = selected)
      }
    }
  }
}

@Composable
private fun MainTabItem(item: MainTabItem, selected: Boolean, modifier: Modifier = Modifier) {
  val alpha by animateFloatAsState(targetValue = if (selected) 1f else ContentAlpha.medium)
  Icon(
    modifier = modifier.alpha(alpha),
    contentDescription = item.title,
    imageVector = item.icon,
  )
}
