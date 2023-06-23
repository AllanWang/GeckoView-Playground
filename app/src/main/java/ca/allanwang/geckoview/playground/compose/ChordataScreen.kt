package ca.allanwang.geckoview.playground.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.ChordataFragment
import ca.allanwang.geckoview.playground.components.UseCases
import ca.allanwang.geckoview.playground.extension.ChordataExtension
import ca.allanwang.geckoview.playground.extension.ExtensionModelConverter
import ca.allanwang.geckoview.playground.hilt.ChordataComponents
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import mozilla.components.browser.state.helper.Target
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
fun ChordataScreen2(modifier: Modifier = Modifier) {
  val vm: ChordataViewModel = viewModel()

  val navItems = remember {
    listOf(
      TabData(title = "Title1", icon = Icons.Default.Add, key = ChordataFragment.FACEBOOK_M_URL),
      TabData(title = "Title2", icon = Icons.Default.BugReport, key = ChordataFragment.GITHUB_URL),
    )
  }

  LaunchedEffect(Unit) {
   vm.  useCases.homeTabs.createHomeTabs(vm.contextId, 0, navItems.map { it.key }) // key is url
  }

  var navSelectedIndex by remember { mutableIntStateOf(0) }

  MainScreenContainer(
    navItems = navItems,
    navSelectedIndex = navSelectedIndex,
    navOnSelect = { navSelectedIndex = it },
  ) { paddingValues, nestedScrollConnection ->
    Chordata(
      modifier = Modifier.padding(paddingValues).nestedScroll(nestedScrollConnection),
      engine = vm.engine,
      store = vm.store,
      target = Target.SelectedTab
    )
  }
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

    //    vm.store.dispatch(
    //      ContainerAction.AddContainerAction(
    //        ContainerState(
    //          contextId = "firefox-container-context2",
    //          name = "firefox-container-context2",
    //          color = ContainerState.Color.BLUE,
    //          icon = ContainerState.Icon.BRIEFCASE,
    //        )
    //      )
    //    )

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
    }
  )
}
