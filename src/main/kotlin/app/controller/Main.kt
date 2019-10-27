package app.controller

import rsa.RSA
import tornadofx.Controller
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger

class Main : Controller() {

    private lateinit var rsa: RSA

    lateinit var publicKey: Pair<BigInteger, BigInteger>
    lateinit var privateKey: Pair<BigInteger, BigInteger>

    fun generate(numBits: Int, iterations: Int, algorithm: String) {
        rsa = RSA(numBits, iterations, algorithm)
        publicKey = rsa.publicKey
        privateKey = rsa.privateKey
    }

    fun encrypt(input: File, output: File) = rsa.encrypt(FileInputStream(input), FileOutputStream(output))
    fun decrypt(input: File, output: File) = rsa.decrypt(FileInputStream(input), FileOutputStream(output))
}