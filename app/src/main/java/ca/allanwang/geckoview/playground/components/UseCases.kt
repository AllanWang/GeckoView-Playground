package ca.allanwang.geckoview.playground.components

import android.content.Context
import com.google.common.flogger.FluentLogger
import mozilla.components.feature.app.links.AppLinksUseCases
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases

class UseCases(private val context: Context, private val core: Core) {

  private val store
    get() = core.store

  val sessionUseCases: SessionUseCases by lazy { SessionUseCases(store) }

  val tabsUseCases: TabsUseCases by lazy { TabsUseCases(store) }

  val contextMenuUseCases: ContextMenuUseCases by lazy { ContextMenuUseCases(store) }

  val downloadsUseCases: DownloadsUseCases by lazy { DownloadsUseCases(store) }

  val appLinksUseCases: AppLinksUseCases by lazy { AppLinksUseCases(context) }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
