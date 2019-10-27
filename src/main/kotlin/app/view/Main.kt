package app.view

import app.controller.Main
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import rsa.simplicityAlgorithm
import tornadofx.*
import java.io.File
import kotlin.system.measureTimeMillis

class Main : View() {

    val bits = SimpleIntegerProperty()
    val iterations = SimpleIntegerProperty()
    val algorithm = SimpleStringProperty()
    val publicKey = SimpleStringProperty()
    val privateKey = SimpleStringProperty()
    val generationTime = SimpleStringProperty()

    //todo observable files value
    //todo check all invalid states
    val input = SimpleStringProperty()
    val output = SimpleStringProperty()
    val handleTime = SimpleStringProperty()
    val handleTimeField = field { text(handleTime) }

    val controller: Main by inject()

    override val root = form {
        fieldset("Key generation") {
            field("bits") {
                textfield(bits) {
                    filterInput { it.controlNewText.isInt() }
                }
            }
            field("iterations") {
                textfield(iterations) {
                    filterInput { it.controlNewText.isInt() }
                }
            }
            field("algorithm") {
                combobox(property = algorithm, values = simplicityAlgorithm.keys.toList()) {
                    selectionModel.selectFirst()
                }
            }
            field("public key") {
                vbox(spacing = 10) {
                    scrollpane { text(publicKey) }
                    button("copy") {
                        action {
                            clipboard.putString(publicKey.value)
                        }
                    }
                }
            }
            field("private key") {
                vbox(spacing = 10) {
                    scrollpane { text(privateKey) }
                    button("copy") {
                        action {
                            clipboard.putString(privateKey.value)
                        }
                    }
                }
            }
            button("generate") {
                action {
                    generationTime.value = ""
                    runAsyncWithProgress {
                        measureTimeMillis {
                            controller.generate(
                                bits.value,
                                iterations.value,
                                algorithm.value
                            )
                        }.let { generationTime.value = "$it ms" }

                        publicKey.value = controller.publicKey.toString()
                        privateKey.value = controller.privateKey.toString()
                    }
                }
            }
            field("generation time") { text(generationTime) }
        }

        fieldset("Message handle") {
            field("input") {
                textfield(input)
                button("...") {
                    action {
                        input.value = FileChooser().showOpenDialog(this@Main.currentWindow).toString()
                    }
                }
            }
            field("output") {
                textfield(output)
                button("...") {
                    action {
                        output.value = FileChooser().showOpenDialog(this@Main.currentWindow).toString()
                    }
                }
            }
            hbox(spacing = 10) {
                button("encrypt") {
                    action {
                        handleTime.value = ""
                        Platform.runLater { handleTimeField.text = "encryption time" }
                        runAsyncWithProgress {
                            measureTimeMillis {
                                controller.encrypt(File(input.value), File(output.value))
                            }.let { handleTime.value = "$it ms" }
                        }
                    }
                }
                button("decrypt") {
                    action {
                        handleTime.value = ""
                        Platform.runLater { handleTimeField.text = "decryption time" }
                        runAsyncWithProgress {
                            measureTimeMillis {
                                controller.decrypt(File(input.value), File(output.value))
                            }.let { handleTime.value = "$it ms" }
                        }
                    }
                }
            }
            this += handleTimeField
        }
    }
}