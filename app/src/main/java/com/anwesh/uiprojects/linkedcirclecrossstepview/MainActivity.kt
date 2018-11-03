package com.anwesh.uiprojects.linkedcirclecrossstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circlecrossstepview.CircleCrossStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircleCrossStepView.create(this)
    }
}
