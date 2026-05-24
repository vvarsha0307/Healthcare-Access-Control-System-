package com.example.demo.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2Util {

    private static final Argon2 argon2 = Argon2Factory.create();

    public static String hashPassword(String password) {
        return argon2.hash(2, 65536, 1, password.toCharArray());
    }

    public static boolean verifyPassword(String hash, String password) {
        return argon2.verify(hash, password.toCharArray());
    }
}