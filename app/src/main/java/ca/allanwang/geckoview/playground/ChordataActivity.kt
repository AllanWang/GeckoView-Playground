package ca.allanwang.geckoview.playground

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChordataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, ChordataFragment())
            .commit()
    }

    override fun onBackPressed() {
        if ((supportFragmentManager.findFragmentById(android.R.id.content) as? ChordataFragment)?.onBackPressed() == true)
            return
        super.onBackPressed()
    }
}