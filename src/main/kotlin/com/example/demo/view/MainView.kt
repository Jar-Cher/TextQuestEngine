package com.example.demo.view

import com.example.demo.app.Styles
import com.google.gson.Gson
import groovy.util.Eval
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    override val root = hbox {
        label(title) {
            addClass(Styles.heading)
        }
    }
}

fun abc() {
    println(Eval.x(4, "2*x"))

    val gson = Gson()
}