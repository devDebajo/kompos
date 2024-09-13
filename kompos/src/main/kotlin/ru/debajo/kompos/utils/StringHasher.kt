package ru.debajo.kompos.utils

import java.security.MessageDigest

internal object StringHasher {
    private val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

    fun hash(input: String): String {
        return bytesToHex(digest.digest(input.toByteArray()))
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}

internal fun String.hash(): String = StringHasher.hash(this)
