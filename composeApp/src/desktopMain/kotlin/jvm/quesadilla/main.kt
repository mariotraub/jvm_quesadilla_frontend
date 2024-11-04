package jvm.quesadilla

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "jvm_quesadilla_frontend",
    ) {
        App()
    }
}