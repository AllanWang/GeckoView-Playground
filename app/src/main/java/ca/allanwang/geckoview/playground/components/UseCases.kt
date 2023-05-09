package ca.allanwang.geckoview.playground.components

import ca.allanwang.geckoview.playground.components.usecases.FloatingTabsUseCases
import ca.allanwang.geckoview.playground.components.usecases.HomeTabsUseCases
import javax.inject.Inject
import javax.inject.Singleton
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases

/**
 * Collection of use cases.
 *
 * Note that included use cases are not lazily loaded.
 */
@Singleton
class UseCases
@Inject
internal constructor(
  val session: SessionUseCases,
  val tabs: TabsUseCases,
  val homeTabs: HomeTabsUseCases,
  val floatingTabs: FloatingTabsUseCases,
)
