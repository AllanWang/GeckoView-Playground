package ca.allanwang.geckoview.playground.compose

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.components.UseCases
import ca.allanwang.geckoview.playground.features.ChordataExtensionFeature
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
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

  DisposableEffect(vm.store) {
    val feature =
      ChordataExtensionFeature(
        runtime = vm.engine,
        store = vm.store,
      )

    feature.start()

    onDispose { feature.stop() }
  }
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
