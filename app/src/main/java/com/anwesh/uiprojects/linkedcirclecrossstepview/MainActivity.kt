package com.anwesh.uiprojects.linkedcirclecrossstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.anwesh.uiprojects.circlecrossstepview.CircleCrossStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view : CircleCrossStepView = CircleCrossStepView.create(this)
        fullScreen()
        view.addOnAnimationListener({createToast("animation $it is complete")}, { createToast("animation $it is reset")})
    }

    fun createToast(txt : String) {
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show()
    }
}

fun MainActivity.fullScreen() {
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}