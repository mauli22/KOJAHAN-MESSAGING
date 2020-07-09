package com.android.koejahan.CryptoLib;

import com.android.koejahan.data.StaticConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HASH {

    public static String SHA512(String pesan){
        String hashPesan = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(StaticConfig.KEY_HASH.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(pesan.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++){
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hashPesan = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashPesan;
    }

}
