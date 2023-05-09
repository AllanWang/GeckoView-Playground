package ca.allanwang.geckoview.playground.compose

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.FACEBOOK_M_URL
import ca.allanwang.geckoview.playground.components.UseCases
import ca.allanwang.geckoview.playground.extension.ChordataExtension
import ca.allanwang.geckoview.playground.extension.ExtensionModelConverter
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
import ca.allanwang.geckoview.playground.launchFloatingUrl
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
    val useCases: UseCases,
    val extensionModelConverter: ExtensionModelConverter,
) : ViewModel() {

  val context1 = "context1"

  val context2 = "context2"

  var contextId by mutableStateOf(context2)

  var tabIndex: Int by mutableStateOf(0)
}

@Composable
fun ChordataScreen(modifier: Modifier = Modifier) {
  val vm: ChordataViewModel = viewModel()

  DisposableEffect(vm.store) {
    val feature =
        ChordataExtension(
            runtime = vm.engine,
            store = vm.store,
            converter = vm.extensionModelConverter,
        )

    feature.start()

    onDispose { feature.stop() }
  }

  val context = LocalContext.current

  MainScreen(
      modifier = modifier,
      engine = vm.engine,
      store = vm.store,
      contextId = vm.contextId,
      useCases = vm.useCases,
      tabIndex = vm.tabIndex,
      onTabSelect = { selectedIndex ->
        if (selectedIndex == vm.tabIndex) {
          vm.useCases.homeTabs.reloadTab(selectedIndex)
//          context.launchFloatingUrl(FACEBOOK_M_URL)
        } else {
          // Change? What if previous selected tab is not home tab
          vm.useCases.homeTabs.selectHomeTab(selectedIndex)
          vm.tabIndex = selectedIndex
        }
      })
}
