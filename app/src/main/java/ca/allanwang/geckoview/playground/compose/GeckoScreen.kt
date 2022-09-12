package ca.allanwang.geckoview.playground.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.allanwang.geckoview.playground.ChordataComponents
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.FACEBOOK_M_URL
import ca.allanwang.geckoview.playground.ChordataFragment.Companion.GITHUB_URL
import ca.allanwang.geckoview.playground.chordata.ChordataSession
import org.mozilla.geckoview.GeckoSessionSettings


class GeckoViewModel(private val components: ChordataComponents) : ViewModel() {

    val session1: ChordataSession = createSession(contextId = "context1", startUrl = FACEBOOK_M_URL)

    val session2: ChordataSession = createSession(contextId = "context2", startUrl = GITHUB_URL)

    var tabIndex: Int by mutableStateOf(0)

    val initSession: (ChordataSession) -> Unit = { session ->
//        session.open(components.core.runtime)
        session.autofillDelegate
    }

    private fun createSession(contextId: String, startUrl: String): ChordataSession =
        ChordataSession(
            GeckoSessionSettings.Builder()
                .allowJavascript(true)
                .displayMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP).contextId(contextId)
                .build()
        ).apply {
            open(components.core.runtime)
            loadUri(startUrl)
        }

    var useSession1 = true
}

@Suppress("UNCHECKED_CAST")
class GeckoViewModelFactory(private val components: ChordataComponents) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = GeckoViewModel(components) as T
}

enum class GeckoTab(val url: String) {
    Facebook(FACEBOOK_M_URL),
    Github(GITHUB_URL),
}

@Composable
fun GeckoScreen(components: ChordataComponents) {
    val vm: GeckoViewModel = viewModel(factory = GeckoViewModelFactory(components))
    val session = if (vm.tabIndex == 0) vm.session1 else vm.session2

    Column {
        TabRow(selectedTabIndex = vm.tabIndex, indicator = {}) {
            listOf("A", "B").forEachIndexed { i, text ->
                val selected = vm.tabIndex == i
                Tab(selected = selected, onClick = { vm.tabIndex = i }) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = text,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        GeckoEngine(session = session, initSession = vm.initSession)
    }
}