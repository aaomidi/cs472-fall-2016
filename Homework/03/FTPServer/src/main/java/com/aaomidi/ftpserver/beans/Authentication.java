package com.aaomidi.ftpserver.beans;

import com.aaomidi.ftpserver.config.auth.Auth;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

public class Authentication {
    private Auth auth;

    private String username;
    private String password;

    private String numberAlgorithm = "SHA1PRNG";
    private String signatureAlgorithm = "PBKDF2WithHmacSHA256";
    private int saltLen = 32;
    private int saltIterations = 20 * 1000;
    private int desiredKeyLength = 256;

    public Authentication(String username, String password, Auth auth) {
        this.username = username;
        this.password = password;
        this.auth = auth;
        if (auth != null) {
            numberAlgorithm = auth.getNumberAlgorithm();
            signatureAlgorithm = auth.getSignatureAlgorithm();
            saltLen = auth.getSaltLen();
            saltIterations = auth.getSaltIterations();
            desiredKeyLength = auth.getDesiredKeyLen();
        }
    }

    public Auth encrypt() throws Exception {
        assert check();
        assert this.auth == null;

        auth = new Auth(username,
                hash(password),
                saltLen,
                saltIterations,
                desiredKeyLength,
                numberAlgorithm,
                signatureAlgorithm);

        cleanup();
        return auth;
    }

    public boolean verify() throws Exception {
        assert check();
        assert this.auth != null;

        Auth auth = new Auth(username,
                hash(password),
                saltLen,
                saltIterations,
                desiredKeyLength,
                numberAlgorithm,
                signatureAlgorithm);

        cleanup();
        return auth.checkAgainst(this.auth);
    }

    private boolean check() {
        if (password == null) throw new RuntimeException("This authentication file is RIP.");

        return true;
    }

    private void cleanup() {
        password = null;
    }

    private String hash(String password) throws Exception {
        byte[] salt;
        if (auth == null) {
            salt = SecureRandom.getInstance(numberAlgorithm).generateSeed(saltLen);
        } else {
            salt = Base64.decodeBase64(auth.getSalt());
        }
        return Base64.encodeBase64String(salt) + "$" + hash(password, salt);
    }

    private String hash(String password, byte[] salt) throws Exception {
        if (password == null || password.length() == 0)
            throw new RuntimeException("Empty passwords are not supported.");

        SecretKeyFactory f = SecretKeyFactory.getInstance(signatureAlgorithm);
        SecretKey key = f.generateSecret(new PBEKeySpec(
                password.toCharArray(), salt, saltIterations, desiredKeyLength)
        );
        return Base64.encodeBase64String(key.getEncoded());
    }

}
