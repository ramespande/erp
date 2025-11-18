package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String rawPassword, String hash) {
        return BCrypt.checkpw(rawPassword, hash);
    }
}

