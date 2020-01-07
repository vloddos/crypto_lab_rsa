package egsa.app.controller

import egsa.EGSA
import tornadofx.Controller
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Main : Controller() {

    private lateinit var egsa: EGSA

    val publicKey get() = egsa.publicKey
    val privateKey get() = egsa.privateKey

    fun generate(numBits: Int, iterations: Int, algorithm: String) {
        egsa = EGSA(numBits, iterations, algorithm)
    }

    fun sign(messageInput: File, signatureOutput: File) =
        egsa.sign(FileInputStream(messageInput), FileOutputStream(signatureOutput))

    fun checkSignature(messageInput: File, signatureInput: File) =
        EGSA.checkSignature(FileInputStream(messageInput), FileInputStream(signatureInput))
}