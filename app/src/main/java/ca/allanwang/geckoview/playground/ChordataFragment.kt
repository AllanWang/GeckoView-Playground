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
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.downloads.share.ShareDownloadFeature
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.sitepermissions.SitePermissionsRules
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebPushDelegate
import org.mozilla.geckoview.WebPushSubscription
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class ChordataFragment : Fragment() {

    private lateinit var binding: ChordataWebBinding

    @Inject
    @ApplicationContext
    internal lateinit var appContext: Context

    @Inject
    internal lateinit var components: ChordataComponents

    /*
     * While these don't need to be wrapped; it could be useful once we move to fragments
     */
    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarFeature = ViewBoundFeatureWrapper<ToolbarFeature>()
    private val promptFeature = ViewBoundFeatureWrapper<PromptFeature>()

    //    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val shareDownloadFeature = ViewBoundFeatureWrapper<ShareDownloadFeature>()
    private val appLinksFeature = ViewBoundFeatureWrapper<AppLinksFeature>()
    private var sitePermissionsFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChordataWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.init()
    }

    fun <T : LifecycleAwareFeature> ViewBoundFeatureWrapper<T>.set(feature: T) {
        set(feature, this@ChordataFragment, binding.root)
    }

    @SuppressLint("WrongThread")
    private fun ChordataWebBinding.init() {

        sessionFeature.set(
            SessionFeature(
                store = components.store,
                goBackUseCase = components.sessionUseCases.goBack,
                engineView = engineView
            )
        )

        toolbarFeature.set(
            ToolbarFeature(
                toolbar = toolbar,
                store = components.store,
                loadUrlUseCase = components.sessionUseCases.loadUrl
            )
        )

        promptFeature.set(
            PromptFeature(
                fragment = this@ChordataFragment,
                store = components.store,
                fragmentManager = parentFragmentManager,
                onNeedToRequestPermissions = { permissions ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val results = requestPermissions(requireView().context, permissions)
                        promptFeature.get()?.onPermissionsResult(permissions,
                            permissions.map { if (results[it] == true) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED }
                                .toIntArray()
                        )
                    }
                }
            )
        )

        appLinksFeature.set(
            feature = AppLinksFeature(
                appContext,
                store = components.store,
                fragmentManager = parentFragmentManager,
                launchInApp = { true },
                loadUrlUseCase = components.sessionUseCases.loadUrl
            )
        )

        val sitePermissionsRules = SitePermissionsRules(
            notification = SitePermissionsRules.Action.ASK_TO_ALLOW,
            microphone = SitePermissionsRules.Action.ASK_TO_ALLOW,
            location = SitePermissionsRules.Action.ASK_TO_ALLOW,
            camera = SitePermissionsRules.Action.ASK_TO_ALLOW,
            autoplayAudible = SitePermissionsRules.AutoplayAction.BLOCKED,
            autoplayInaudible = SitePermissionsRules.AutoplayAction.ALLOWED,
            persistentStorage = SitePermissionsRules.Action.BLOCKED,
            mediaKeySystemAccess = SitePermissionsRules.Action.BLOCKED
        )
        sitePermissionsFeature.set(
            feature = SitePermissionsFeature(
                context = appContext,
                fragmentManager = parentFragmentManager,
                onNeedToRequestPermissions = {
                    // This it will be always empty because we are not asking for user input
                },
                onShouldShowRequestPermissionRationale = {
                    // Since we don't request permissions this it will not be called
                    false
                },
                sitePermissionsRules = sitePermissionsRules,
                store = components.store
            )
        )

        val messageDelegate = object : WebExtension.MessageDelegate {
            override fun onMessage(
                nativeApp: String,
                message: Any,
                sender: WebExtension.MessageSender
            ): GeckoResult<Any>? {
                logger.atInfo().log("%s", message)
                return null
            }
        }

//        components.runtime.webExtensionController.ensureGeckoTestBuiltIn().accept { extension ->
//            session.webExtensionController.setMessageDelegate(
//                extension!!,
//                messageDelegate,
//                "browser"
//            )
//        }

        components.runtime.webPushController.delegate = object : WebPushDelegate {
            override fun onSubscribe(
                scope: String,
                appServerKey: ByteArray?
            ): GeckoResult<WebPushSubscription>? {
                logger.atInfo().log("onSubscribe %s: %s", scope, appServerKey)
                return super.onSubscribe(scope, appServerKey)
            }

            override fun onGetSubscription(scope: String): GeckoResult<WebPushSubscription>? {
                logger.atInfo().log("onGetSubscription %s", scope)
                return super.onGetSubscription(scope)
            }
        }

//        session.permissionDelegate = object : GeckoSession.PermissionDelegate {
//            override fun onAndroidPermissionsRequest(
//                session: GeckoSession,
//                permissions: Array<out String>?,
//                callback: GeckoSession.PermissionDelegate.Callback
//            ) {
//                if (permissions == null) {
//                    callback.grant()
//                    return
//                }
//                val missingPermissions: List<String> = permissions
//                    .filter {
//                        ContextCompat.checkSelfPermission(
//                            this@ChordataActivity,
//                            it
//                        ) != PackageManager.PERMISSION_GRANTED
//                    }
//                if (missingPermissions.isEmpty()) {
//                    callback.grant()
//                    return
//                }
//                val requestPermissionLauncher =
//                    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult: Map<String, Boolean> ->
//                        if (permissionsResult.values.all { it })
//                            callback.grant()
//                        else
//                            callback.reject()
//                    }
//                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
//            }
//
//            override fun onContentPermissionRequest(
//                session: GeckoSession,
//                perm: GeckoSession.PermissionDelegate.ContentPermission
//            ): GeckoResult<Int>? {
//                logger.atInfo().log("Requesting permission %s %s", perm.contextId, perm.uri)
//                return GeckoResult.fromValue(GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW)
//            }
//
//            override fun onMediaPermissionRequest(
//                session: GeckoSession,
//                uri: String,
//                video: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
//                audio: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
//                callback: GeckoSession.PermissionDelegate.MediaCallback
//            ) {
//                super.onMediaPermissionRequest(session, uri, video, audio, callback)
//            }
//        }

        components.sessionUseCases.loadUrl(NOTIFICATION_DEMO_URL)

        listOf(toolbarFeature, sessionFeature).asSequence()
            .map { it.get() ?: throw IllegalStateException("Feature not set") }
            .forEach {
                lifecycle.addObserver(it)
            }
    }

    /**
     * Upon request, returns a map of previously missing permissions to their respective results.
     */
    private suspend fun requestPermissions(
        context: Context,
        permissions: Array<String>
    ): Map<String, Boolean> {
        val missingPermissions: List<String> = permissions
            .filter {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }
        if (missingPermissions.isEmpty()) return emptyMap()

        return suspendCoroutine { continuation ->
            val requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult: Map<String, Boolean> ->
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


    companion object {
        private val logger = FluentLogger.forEnclosingClass()
        private const val GITHUB_URL = "https://github.com/AllanWang"
        private const val NOTIFICATION_DEMO_URL = "https://pushalert.co/demo"
    }
}