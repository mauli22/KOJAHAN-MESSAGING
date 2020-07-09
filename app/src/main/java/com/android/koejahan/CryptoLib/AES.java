package com.android.koejahan.CryptoLib;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private static final String ALGORITMAENKRIPSI = "AES";

    public static SecretKey buatkunci() throws NoSuchAlgorithmException {
        SecureRandom sr = new SecureRandom();//buat angka acak
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITMAENKRIPSI);//generate key
        kg.init(128, sr);//membuat key AES 256 bit
        return kg.generateKey();
    }

    public static byte[] AESEncryption(String plainText, SecretKey secretKey, byte[] iv) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        //cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plainText.getBytes());
    }
    public static String AESDecryption(byte[] CipherText, SecretKey secretKey, byte[] iv) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        //cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] result = cipher.doFinal(CipherText);
        return new String(result);
    }
    public static byte[] createInitializationVector(){ //untuk mencegah pattern attack
        byte[] iv = new byte[16];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(iv);
        return iv;
    }

}
