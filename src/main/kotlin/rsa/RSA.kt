package rsa

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.util.*

class RSA {

    private val modulus: BigInteger
    private val modulusByteLength: Int
    private val numberBytesReadFromSource: Int

    private val publicExp: BigInteger
    private val privateExp: BigInteger

    val publicKey get() = Pair(publicExp, modulus)
    val privateKey get() = Pair(privateExp, modulus)

    constructor(modulus: BigInteger, publicExp: BigInteger, privateExp: BigInteger) {
        this.modulus = modulus
        modulusByteLength = modulus.bitLength() / Byte.SIZE_BITS + 1
        numberBytesReadFromSource = modulusByteLength - 2
        this.publicExp = publicExp
        this.privateExp = privateExp
    }

    constructor(numBits: Int, iterations: Int, algorithm: String) {
        val p = randomProbablyPrime(numBits, iterations, algorithm)
        val q = randomProbablyPrime(numBits, iterations, algorithm)
        modulus = p * q
        modulusByteLength = modulus.bitLength() / Byte.SIZE_BITS + 1
        numberBytesReadFromSource = modulusByteLength - 2

        val l = (p - ONE) * (q - ONE) / (p - ONE).gcd(q - ONE)//???
        val r = Random()
        while (true) {
            val e = BigInteger(l.bitLength(), r) % (l - TWO) + TWO
            if (e.gcd(l) == ONE) {
                publicExp = e
                break
            }
        }
        privateExp = publicExp.modInverse(l)//>0???
    }

    //available???????????
    fun encrypt(inputStream: InputStream, outputStream: OutputStream) {
        inputStream.use { `is` ->
            outputStream.use { os ->
                while (`is`.available() >= numberBytesReadFromSource) {
                    var bytes = `is`.readNBytes(numberBytesReadFromSource).apply {
                        if (size >= modulusByteLength)
                            println("read bytes size $size")
                    }
                    val number = BigInteger(byteArrayOf(0, *bytes))
                    bytes = number.modPow(publicExp, modulus).toByteArray().apply {
                        if (size > modulusByteLength) {
                            modulus.toByteArray().run {
                                println("modulus bytes size $size")
                                println("modulus bytes ${Arrays.toString(this)}")
                            }
                            println("encrypted bytes size $size")
                            println("encrypted bytes ${Arrays.toString(this)}")
                        }
                    }
                    os.write(ByteArray(modulusByteLength - bytes.size) { 0 } + bytes)
                }

                if (`is`.available() > 0) {
                    var bytes = `is`.readBytes()
                    val numberLastBytes = bytes.size
                    val number = BigInteger(byteArrayOf(0, *bytes))
                    bytes = number.modPow(publicExp, modulus).toByteArray()
                    os.write(ByteArray(modulusByteLength - bytes.size) { 0 } + bytes)
                    os.flush()
                    DataOutputStream(os).use { it.writeInt(numberLastBytes) }
                }
            }
        }
        println("modulus byte length $modulusByteLength")
    }

    fun decrypt(inputStream: InputStream, outputStream: OutputStream) {
        inputStream.use { `is` ->
            outputStream.use { os ->
                while (`is`.available() > modulusByteLength + Int.SIZE_BYTES) {//assumed modulusByteLength>Int.SIZE_BYTES
                    var bytes = `is`.readNBytes(modulusByteLength)
                    val number = BigInteger(bytes)
                    bytes = number.modPow(privateExp, modulus).toByteArray()
                    when {
                        bytes.size < numberBytesReadFromSource ->
                            bytes = ByteArray(numberBytesReadFromSource - bytes.size) { 0 } + bytes
                        bytes.size == numberBytesReadFromSource + 1 -> bytes = bytes.sliceArray(1..bytes.lastIndex)
                    }
                    os.write(bytes)
                }

                var bytes = `is`.readNBytes(modulusByteLength)
                val number = BigInteger(bytes)
                bytes = number.modPow(privateExp, modulus).toByteArray()

                val numberLastBytes =
                    if (`is`.available() > 0) DataInputStream(`is`).use { it.readInt() } else numberBytesReadFromSource

                when {
                    bytes.size < numberLastBytes ->
                        bytes = ByteArray(numberLastBytes - bytes.size) { 0 } + bytes
                    bytes.size == numberLastBytes + 1 -> bytes = bytes.sliceArray(1..bytes.lastIndex)
                }

                os.write(bytes)
            }
        }
        println("modulus byte length $modulusByteLength")
    }
}