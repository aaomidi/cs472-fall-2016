package com.aaomidi.ftpserver.config.auth;

import lombok.Data;

@Data
public class Auth {
    private final String username;
    /*
     * Hashed password. Cleartext password will be thrown out the second its hashed.
     */
    private final String hash;

    private final int saltLen;
    private final int saltIterations;
    private final int desiredKeyLen;
    private final String numberAlgorithm;
    private final String signatureAlgorithm;

    public boolean checkAgainst(Auth auth) {
        assert auth != null;

        String u1 = this.getUsername();
        String u2 = auth.getUsername();

        String p1 = this.getPassword();
        String p2 = auth.getPassword();

        if (p1 == null || p2 == null) return false;
        if (u1 == null || u2 == null) return false;

        return p1.equals(p2) && u1.equals(u2);
    }

    public String getSalt() {
        String[] saltyHash = getSaltyHash();
        return saltyHash[0];
    }

    public String getPassword() {
        String[] saltyHash = getSaltyHash();
        return saltyHash[1];
    }

    private String[] getSaltyHash() {
        String[] saltyHash = hash.split("\\$");
        if (saltyHash.length != 2) {
            throw new RuntimeException("Hash was invalid");
        }
        return saltyHash;
    }
}
