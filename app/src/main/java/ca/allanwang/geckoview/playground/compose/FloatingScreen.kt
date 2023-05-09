package ca.allanwang.geckoview.playground.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.components.UseCases
import ca.allanwang.geckoview.playground.components.usecases.FloatingTabsUseCases
import ca.allanwang.geckoview.playground.extension.ExtensionModelConverter
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import mozilla.components.browser.state.helper.Target
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine

@HiltViewModel
class ChordataFloatingViewModel
@Inject
internal constructor(
    val components: ChordataComponents,
    val engine: Engine,
    val store: BrowserStore,
    val useCases: UseCases,
) : ViewModel() {
  var baseUrl: String by mutableStateOf("")
}

@Composable
fun FloatingScreen(modifier: Modifier = Modifier) {
  val vm: ChordataFloatingViewModel = viewModel()

  LaunchedEffect(vm.baseUrl) { vm.useCases.floatingTabs.createFloatingTab(vm.baseUrl) }

  Chordata(
      modifier = modifier,
      engine = vm.engine,
      store = vm.store,
      target = Target.Tab(FloatingTabsUseCases.TAB_ID))
}
