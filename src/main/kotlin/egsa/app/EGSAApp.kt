package egsa.app

import egsa.app.view.Main
import tornadofx.App
import tornadofx.launch

class EGSAApp : App(Main::class)

fun main(args: Array<String>) {
    launch<EGSAApp>(args)
}