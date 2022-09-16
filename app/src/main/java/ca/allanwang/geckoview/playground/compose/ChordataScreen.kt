package ca.allanwang.geckoview.playground.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.ChordataComponents
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.FACEBOOK_M_URL
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.GITHUB_URL
import mozilla.components.browser.state.helper.Target
import mozilla.components.compose.engine.WebContent

class ChordataViewModel(val components: ChordataComponents) : ViewModel() {

  val context1 = "context1"

  val context2 = "context2"

  var contextId by mutableStateOf(context1)

  var tabIndex: Int by mutableStateOf(0)
}

@Suppress("UNCHECKED_CAST")
class ChordataViewModelFactory(private val components: ChordataComponents) :
    ViewModelProvider.NewInstanceFactory() {
  override fun <T : ViewModel> create(modelClass: Class<T>): T = ChordataViewModel(components) as T
}

@Composable
fun ChordataScreen(components: ChordataComponents) {
  val vm: ChordataViewModel = viewModel(factory = ChordataViewModelFactory(components))

  val contextId = vm.contextId
  val engine = vm.components.core.engine
  val store = vm.components.core.store
  val tabsUseCases = vm.components.useCases.tabsUseCases

  val produceTabs: () -> List<String> = remember {
    {
      tabsUseCases.removeAllTabs()
      val tab1 = tabsUseCases.addTab(url = FACEBOOK_M_URL, contextId = contextId)
      val tab2 = tabsUseCases.addTab(url = GITHUB_URL, contextId = contextId)
      val result = listOf(tab1, tab2)
      println("New tabs $result")
      result
    }
  }

  val tabs by produceState(initialValue = produceTabs(), vm.contextId) { value = produceTabs() }

  val target = Target.Tab(tabs.getOrNull(vm.tabIndex) ?: "invalid")

  SideEffect { println("Target ${target.tabId}") }

  Column {
    TabRow(selectedTabIndex = vm.tabIndex, indicator = {}) {
      listOf("A", "B").forEachIndexed { i, text ->
        val selected = vm.tabIndex == i
        Tab(selected = selected, onClick = { vm.tabIndex = i }) {
          Text(
              modifier = Modifier.padding(16.dp),
              text = text,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
      }
    }
    WebContent(engine = engine, store = store, target = target)
  }
}
