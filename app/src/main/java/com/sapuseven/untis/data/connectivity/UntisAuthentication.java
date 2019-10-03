package com.sapuseven.untis.data.connectivity;

import com.sapuseven.untis.models.untis.UntisAuth;

import org.apache.commons.codec.binary.Base32;
import org.joda.time.DateTime;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class UntisAuthentication {
	private static int verifyCode(byte[] key, long t) throws InvalidKeyException, NoSuchAlgorithmException {
		int j = 0;

		byte[] arrayOfByte = new byte[8];
		int i = 8;

		while (--i > 0) {
			arrayOfByte[i] = (byte) t;
			t >>>= 8;
		}

		Mac localMac = Mac.getInstance("HmacSHA1");
		localMac.init(new SecretKeySpec(key, "HmacSHA1"));
		key = localMac.doFinal(arrayOfByte);
		int k = key[19];
		t = 0L;
		i = j;
		while (i < 4) {
			long l = key[((k & 0xF) + i)] & 0xFF;
			i += 1;
			t = t << 8 | l;
		}
		return (int) ((t & 0x7FFFFFFF) % 1000000L);
	}

	private static long createTimeBasedCode(long timestamp, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
		if ((secret != null) && (!secret.isEmpty()))
			return verifyCode(new Base32().decode(secret.toUpperCase(Locale.ROOT).getBytes()), timestamp / 30000L); // Code will change all 30000 milliseconds

		return 0L;
	}

	public static UntisAuth getAuthObject(String user, String key) throws InvalidKeyException, NoSuchAlgorithmException {
		long currentTimeMillis = new DateTime().getMillis();
		return new UntisAuth(user, createTimeBasedCode(currentTimeMillis, key), currentTimeMillis);
	}

	public static UntisAuth getAnonymousAuthObject() {
		return new UntisAuth("#anonymous#", 0, new DateTime().getMillis());
	}
}
