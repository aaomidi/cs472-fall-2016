package com.aaomidi.ftpserver.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class SecurityUtils {
    private static final String NUMBER_ALG = "SHA1PRNG";
    private static final String SIGNATURE_ALG = "PBKDF2WithHmacSHA256";
    private static final int SALT_LEN = 32;
    private static final int ITERATIONS = 20 * 1000;
    private static final int DESIRED_KEY_LEN = 256;

    public static String hash(String password) throws Exception {
        byte[] salt = SecureRandom.getInstance(NUMBER_ALG).generateSeed(SALT_LEN);
        return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
    }

    private static String hash(String password, byte[] salt) throws Exception {
        if (password == null || password.length() == 0)
            throw new IllegalArgumentException("Empty passwords are not supported.");
        SecretKeyFactory f = SecretKeyFactory.getInstance(SIGNATURE_ALG);
        SecretKey key = f.generateSecret(new PBEKeySpec(
                password.toCharArray(), salt, ITERATIONS, DESIRED_KEY_LEN)
        );
        return Base64.encodeBase64String(key.getEncoded());
    }

}
