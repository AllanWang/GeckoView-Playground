package ca.allanwang.geckoview.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.allanwang.geckoview.playground.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val session = GeckoSession()

    @Inject
    lateinit var runtime: GeckoRuntime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.init()
    }

    private fun ActivityMainBinding.init() {
        session.open(runtime)
        geckoView.setSession(session)
        session.loadUri("https://github.com/AllanWang")
    }

    override fun onBackPressed() {
        session.goBack()
        super.onBackPressed()
    }
}