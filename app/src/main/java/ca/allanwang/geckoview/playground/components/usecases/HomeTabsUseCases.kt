package ca.allanwang.geckoview.playground.components.usecases

import javax.inject.Inject
import javax.inject.Singleton
import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.store.BrowserStore

@Singleton
class HomeTabsUseCases @Inject internal constructor(private val store: BrowserStore) {

  fun createHomeTabs(
    contextId: String,
    selectedIndex: Int,
    urls: List<String>
  ): List<TabSessionState> {
    store.dispatch(TabListAction.RemoveAllTabsAction())
    if (urls.isEmpty()) return emptyList()
    val tabs =
      urls.mapIndexed { i, url -> createTab(id = tabId(i), url = url, contextId = contextId) }
    store.dispatch(TabListAction.AddMultipleTabsAction(tabs))
    // Preload all tabs
    for (tab in tabs) {
      store.dispatch(EngineAction.LoadUrlAction(tab.id, tab.content.url))
    }
    selectHomeTab(selectedIndex)
    return tabs
  }

  /**
   * Select home tab based on index.
   *
   * If the index is OOB, the selected tab will be null.
   */
  fun selectHomeTab(index: Int) {
    store.dispatch(TabListAction.SelectTabAction(tabId(index)))
  }

  fun reloadTab(index: Int) {
    store.dispatch(EngineAction.ReloadAction(tabId(index)))
  }

  private fun tabId(index: Int) = "$PREFIX--$index"

  companion object {
    private const val PREFIX = "gecko-playground-home"
  }
}
