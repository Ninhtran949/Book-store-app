package com.example.Sachpee.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil {

    public static SecretKey generateKeyFromPassword(String password) throws NoSuchAlgorithmException {
        // Sử dụng SHA-256 để tạo khóa từ mật khẩu đã hash
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes());
        key = Arrays.copyOf(key, 16); // Sử dụng 16 byte đầu tiên cho khóa AES
        return new SecretKeySpec(key, "AES");
    }
}