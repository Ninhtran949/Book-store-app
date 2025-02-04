package com.example.Sachpee.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

public class SecurePreferences {

    private static final String PREFS_NAME = "My_User";

    public static SharedPreferences getEncryptedSharedPreferences(Context context, String passwordHash) throws GeneralSecurityException, IOException {
        // Tạo khóa AES từ mật khẩu đã hash
        SecretKey secretKey = EncryptionUtil.generateKeyFromPassword(passwordHash);

        // Sử dụng EncryptedSharedPreferences để mã hóa dữ liệu
        return EncryptedSharedPreferences.create(
                PREFS_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
}