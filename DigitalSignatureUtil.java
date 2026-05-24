package com.example.demo.security;
import java.security.MessageDigest;
public class DigitalSignatureUtil {
    public static String sign(String data, String secret) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = data + secret;
            byte[] hash = md.digest(input.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hash)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

