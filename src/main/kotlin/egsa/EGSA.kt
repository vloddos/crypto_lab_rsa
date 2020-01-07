package egsa


import THREE
import randomProbablyPrime
import java.io.*
import java.math.BigInteger
import java.math.BigInteger.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

private val random = Random()

class EGSA(val pNumBits: Int, iterations: Int, algorithm: String) {

    data class Signature(
        val p: BigInteger,
        val g: BigInteger,
        val y: BigInteger,
        val a: BigInteger,
        val b: BigInteger
    ) : Serializable

    private val p = randomProbablyPrime(pNumBits, iterations, algorithm)
    private val g = BigInteger(pNumBits, random) % (p - TWO) + TWO
    private val x = BigInteger(pNumBits, sr) % (p - THREE) + TWO
    private val y = g.modPow(x, p)

    val publicKey get() = Triple(p, g, y)
    val privateKey get() = x

    companion object {

        private val md = MessageDigest.getInstance("SHA-1")
        private val sr = SecureRandom()

        fun checkSignature(messageInputStream: InputStream, signatureInputStream: InputStream): Boolean {
            val (p, g, y, a, b) =
                ObjectInputStream(signatureInputStream).use { it.readObject() as Signature }

            val m = BigInteger(
                md.digest(
                    messageInputStream.use { it.readAllBytes() }
                )
            ) % (p - THREE) + TWO

            return ZERO < a && a < p && ZERO < b && b < p - ONE &&
                    y.modPow(a, p) * a.modPow(b, p) % p == g.modPow(m, p)
        }
    }

    private fun generateK(): BigInteger {
        while (true)
            (BigInteger(pNumBits, sr) % (p - THREE) + TWO).run {
                if (gcd(p - ONE) == ONE)
                    return this
            }
    }

    fun sign(messageInputStream: InputStream, signatureOutputStream: OutputStream) {
        val k = generateK()
        val m = BigInteger(
            md.digest(
                messageInputStream.use { it.readAllBytes() }
            )
        ) % (p - THREE) + TWO
        val a = g.modPow(k, p)
        val b = k.modInverse(p - ONE) * ((m - x * a + p * (p - ONE)) % (p - ONE)) % (p - ONE)

        ObjectOutputStream(signatureOutputStream).use { it.writeObject(Signature(p, g, y, a, b)) }
    }
}


fun main() {
    val mf = File("abc.txt")
    val sf = File("s.txt")
    val egsa = EGSA(1024, 40, "Miller-Rabin")
    egsa.sign(FileInputStream(mf), FileOutputStream(sf))
    println(EGSA.checkSignature(FileInputStream(mf), FileInputStream(sf)))
}