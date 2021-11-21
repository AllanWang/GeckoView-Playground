package ca.allanwang.geckoview.playground

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.allanwang.geckoview.playground.databinding.ActivityMainBinding
import com.google.common.flogger.FluentLogger
import dagger.hilt.android.AndroidEntryPoint
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.WebExtension
import javax.inject.Inject

@AndroidEntryPoint
class ChordataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val session = ChordataSession()

    @Inject
    lateinit var runtime: GeckoRuntime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.init()
    }

    @SuppressLint("WrongThread")
    private fun ActivityMainBinding.init() {
        session.open(runtime)

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

        runtime.webExtensionController.ensureGeckoTestBuiltIn().accept { extension ->
            session.webExtensionController.setMessageDelegate(
                extension!!,
                messageDelegate,
                "browser"
            )
        }

        geckoView.setSession(session)
        session.loadUri("https://github.com/AllanWang")
    }

    override fun onBackPressed() {
        if (session.canGoBack)
            session.goBack()
        else
            super.onBackPressed()
    }

    companion object {
        private val logger = FluentLogger.forEnclosingClass()
    }
}