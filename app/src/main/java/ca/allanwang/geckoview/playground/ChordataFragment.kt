package ca.allanwang.geckoview.playground

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ca.allanwang.geckoview.playground.databinding.ChordataWebBinding
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.downloads.share.ShareDownloadFeature
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.sitepermissions.SitePermissionsRules
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class ChordataFragment : Fragment() {

  private var _binding: ChordataWebBinding? = null
  private val binding: ChordataWebBinding
    get() = _binding!!

  @Inject @ApplicationContext internal lateinit var appContext: Context

  @Inject internal lateinit var components: ChordataComponents

  /*
   * While these don't need to be wrapped; it could be useful once we move to fragments
   */
  private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
  private val toolbarFeature = ViewBoundFeatureWrapper<ToolbarFeature>()
  private val promptFeature = ViewBoundFeatureWrapper<PromptFeature>()
  private val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()

  //    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
  private val shareDownloadFeature = ViewBoundFeatureWrapper<ShareDownloadFeature>()
  private val appLinksFeature = ViewBoundFeatureWrapper<AppLinksFeature>()
  private var sitePermissionsFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    _binding = ChordataWebBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.init()
  }

  private fun <T : LifecycleAwareFeature> ViewBoundFeatureWrapper<T>.set(feature: T) {
    set(feature, this@ChordataFragment, binding.root)
  }

  @SuppressLint("WrongThread")
  private fun ChordataWebBinding.init() {

    var sessionId = "context1"

    sessionFeature.set(
        SessionFeature(
            store = components.core.store,
            goBackUseCase = components.useCases.sessionUseCases.goBack,
            engineView = engineView,
            //                tabId = sessionId
            ))

    val tabs = components.core.store.state.tabs.map { it.content.url to it.contextId }
    logger.atInfo().log("Tabs %s", tabs)

    toolbarFeature.set(
        ToolbarFeature(
            toolbar = toolbar,
            store = components.core.store,
            loadUrlUseCase = components.useCases.sessionUseCases.loadUrl))

    components.core.store.dispatch(TabListAction.RemoveAllTabsAction(recoverable = false))
    components.useCases.tabsUseCases.addTab(url = FACEBOOK_M_URL, contextId = sessionId)

    promptFeature.set(
        PromptFeature(
            fragment = this@ChordataFragment,
            store = components.core.store,
            fragmentManager = parentFragmentManager,
            onNeedToRequestPermissions = { permissions ->
              promptFeature.requestPermissions(permissions)
            }))

    swipeRefreshFeature.set(
        SwipeRefreshFeature(
            store = components.core.store,
            reloadUrlUseCase = components.useCases.sessionUseCases.reload,
            swipeRefreshLayout = swipeRefresh))

    appLinksFeature.set(
        feature =
            AppLinksFeature(
                appContext,
                store = components.core.store,
                fragmentManager = parentFragmentManager,
                launchInApp = { true },
                loadUrlUseCase = components.useCases.sessionUseCases.loadUrl))

    val sitePermissionsRules =
        SitePermissionsRules(
            notification = SitePermissionsRules.Action.ASK_TO_ALLOW,
            microphone = SitePermissionsRules.Action.ASK_TO_ALLOW,
            location = SitePermissionsRules.Action.ASK_TO_ALLOW,
            camera = SitePermissionsRules.Action.ASK_TO_ALLOW,
            autoplayAudible = SitePermissionsRules.AutoplayAction.BLOCKED,
            autoplayInaudible = SitePermissionsRules.AutoplayAction.ALLOWED,
            persistentStorage = SitePermissionsRules.Action.BLOCKED,
            mediaKeySystemAccess = SitePermissionsRules.Action.BLOCKED,
            crossOriginStorageAccess = SitePermissionsRules.Action.ASK_TO_ALLOW,
        )
    sitePermissionsFeature.set(
        feature =
            SitePermissionsFeature(
                context = appContext,
                fragmentManager = parentFragmentManager,
                storage = components.core.sitePermissionsStorage,
                onNeedToRequestPermissions = { permissions ->
                  sitePermissionsFeature.requestPermissions(permissions)
                },
                onShouldShowRequestPermissionRationale = { true },
                sitePermissionsRules = sitePermissionsRules,
                store = components.core.store))

    // components.useCases.sessionUseCases.loadUrl(DEFAULT_URL)
  }

  private fun TabsUseCases.switchSessionId(sessionId: String) {
    val store = components.core.store
    val newTabs = store.state.tabs.map { it.copy(contextId = sessionId) }
    store.dispatch(TabListAction.RemoveAllTabsAction())
    store.dispatch(TabListAction.AddMultipleTabsAction(newTabs))
  }

  private fun <T> ViewBoundFeatureWrapper<T>.requestPermissions(permissions: Array<String>) where
  T : LifecycleAwareFeature,
  T : PermissionsFeature {
    get() ?: return
    val context = view?.context ?: return
    viewLifecycleOwner.lifecycleScope.launch {
      val results = requestPermissions(context, permissions)
      get()
          ?.onPermissionsResult(
              permissions,
              permissions
                  .map {
                    if (results[it] == true) PackageManager.PERMISSION_GRANTED
                    else PackageManager.PERMISSION_DENIED
                  }
                  .toIntArray())
    }
  }

  /** Upon request, returns a map of previously missing permissions to their respective results. */
  private suspend fun requestPermissions(
      context: Context,
      permissions: Array<String>
  ): Map<String, Boolean> {
    val missingPermissions: List<String> =
        permissions.filter {
          ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    if (missingPermissions.isEmpty()) return emptyMap()

    return suspendCoroutine { continuation ->
      val requestPermissionLauncher =
          registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
              permissionsResult: Map<String, Boolean> ->
            continuation.resume(permissionsResult)
          }
      requestPermissionLauncher.launch(missingPermissions.toTypedArray())
    }
  }

  fun onBackPressed(): Boolean {
    logger.atInfo().log("Toolbar back")
    if (toolbarFeature.onBackPressed()) return true
    logger.atInfo().log("Session back")
    if (sessionFeature.onBackPressed()) return true
    logger.atInfo().log("Super back")
    return false
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()

    const val GITHUB_URL = "https://github.com/AllanWang"
    const val NOTIFICATION_DEMO_URL = "https://pushalert.co/demo"
    const val FACEBOOK_M_URL = "https://m.facebook.com"
    const val FACEBOOK_M_PUSH_URL = "https://m.facebook.com/settings/notifications/push/"

    const val DEFAULT_URL = FACEBOOK_M_PUSH_URL
  }
}
