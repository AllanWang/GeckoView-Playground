package ca.allanwang.geckoview.playground

import org.mozilla.geckoview.*

class ChordataSession @JvmOverloads constructor(
    settings: GeckoSessionSettings? = null
) : GeckoSession(settings) {

    private var _navigationDelegate: NavigationDelegate? = null
    var canGoBack: Boolean = false
    var canGoForward: Boolean = false

    init {
        super.setNavigationDelegate(object : NavigationDelegate {

            override fun onLocationChange(session: GeckoSession, url: String?) {
                _navigationDelegate?.onLocationChange(session, url)
            }

            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<PermissionDelegate.ContentPermission>
            ) {
                _navigationDelegate?.onLocationChange(session, url, perms)
            }

            override fun onLoadRequest(
                session: GeckoSession,
                request: NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny>? {
                return _navigationDelegate?.onLoadRequest(session, request)
            }

            override fun onSubframeLoadRequest(
                session: GeckoSession,
                request: NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny>? {
                return _navigationDelegate?.onSubframeLoadRequest(session, request)
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession>? {
                return _navigationDelegate?.onNewSession(session, uri)
            }

            override fun onLoadError(
                session: GeckoSession,
                uri: String?,
                error: WebRequestError
            ): GeckoResult<String>? {
                return _navigationDelegate?.onLoadError(session, uri, error)
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                this@ChordataSession.canGoBack = canGoBack
                _navigationDelegate?.onCanGoBack(session, canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                this@ChordataSession.canGoForward = canGoForward
                _navigationDelegate?.onCanGoForward(session, canGoForward)
            }
        })
    }

    override fun setNavigationDelegate(delegate: NavigationDelegate?) {
        this._navigationDelegate = delegate
    }

    override fun getNavigationDelegate(): NavigationDelegate? = _navigationDelegate
}