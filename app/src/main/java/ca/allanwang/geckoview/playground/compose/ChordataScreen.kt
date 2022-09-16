package ca.allanwang.geckoview.playground.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
import ca.allanwang.geckoview.playground.components.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine

@HiltViewModel
class ChordataViewModel
@Inject
internal constructor(
  val components: ChordataComponents,
  val engine: Engine,
  val store: BrowserStore,
  val useCases: UseCases
) : ViewModel() {

  val context1 = "context1"

  val context2 = "context2"

  var contextId by mutableStateOf(context1)

  var tabIndex: Int by mutableStateOf(0)
}

@Composable
fun ChordataScreen() {
  val vm: ChordataViewModel = viewModel()

  MainScreen(
    engine = vm.engine,
    store = vm.store,
    contextId = vm.contextId,
    useCases = vm.useCases,
    tabIndex = vm.tabIndex,
    onTabSelect = {
      vm.tabIndex = it
      // Change? What if previous selected tab is not home tab
      vm.useCases.homeTabs.selectHomeTab(it)
    }
  )
}
