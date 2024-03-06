package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.util.Base32.decode
import kotlinx.serialization.Serializable
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.Clock
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Serializable
data class Auth(
	val user: String,
	val otp: Long,
	val clientTime: Long
) {
	companion object {
		private const val DEFAULT_USER = "#anonymous#"

		@Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
		private fun verifyCode(key: ByteArray, time: Long): Int {
			var t = time

			val arrayOfByte = ByteArray(8)
			var i = 8

			while (--i > 0) {
				arrayOfByte[i] = t.toByte()
				t = t ushr 8
			}

			val localMac = Mac.getInstance("HmacSHA1")
			localMac.init(SecretKeySpec(key, "HmacSHA1"))
			val hashedKey = localMac.doFinal(arrayOfByte)
			val k = hashedKey[19].toInt()
			t = 0L
			i = 0
			while (i < 4) {
				val l = hashedKey[(k and 0xF) + i].toInt() and 0xFF
				i += 1
				t = t shl 8 or l.toLong()
			}
			return ((t and 0x7FFFFFFF) % 1000000L).toInt()
		}

		private fun createTimeBasedCode(timestamp: Long, secret: String?): Long {
			return try {
				if (secret?.isNotEmpty() == true)
					verifyCode(
						decode(secret.uppercase(Locale.ROOT)),
						timestamp / 30000L
					).toLong()
				else
					0L
			} catch (e: Exception) {
				0L
			}
		}
	}

	constructor(user: String? = null, key: String? = null, clock: Clock = Clock.systemDefaultZone()) : this(
		user ?: DEFAULT_USER,
		createTimeBasedCode(clock.millis(), key),
		clock.millis()
	)
}
