package com.android.koejahan.CryptoLib;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.util.Base64Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.crypto.Cipher;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RSA {
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String KEY_ALIAS = "KeyRSASecurity";
    private static final String PUBLIC_KEY_FILE = "FilePublicKey";
    private static final String PRIVATE_KEY_FILE = "FilePrivateKey";

    public static void generateKeyPair(Context context) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        PublicKey pub = pair.getPublic();
        Log.d("PUBLICKEY",pub.toString());
        saveToFilePublic(context, PUBLIC_KEY_FILE, pub);

        PrivateKey priv = pair.getPrivate();
        Log.d("PRIVATEKEY",priv.toString());
        saveToFilePrivate(context, PRIVATE_KEY_FILE, priv);
    }

    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.encodeToString(cipherText, Base64.DEFAULT);
    }

    public static String encryptWithPrivate(String plainText, PrivateKey privkey) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, privkey);

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

        return Base64.encodeToString(cipherText, Base64.DEFAULT);
    }

    public static String decryptWithPublic(String cipherText, PublicKey publicKey) throws Exception{
        byte[] bytes = Base64Utils.decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, publicKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
        byte[] bytes = Base64Utils.decode(cipherText);

        Cipher decriptCipher = Cipher.getInstance("RSA");
        decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decriptCipher.doFinal(bytes), UTF_8);
    }

    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.encodeToString(signature, Base64.DEFAULT);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64Utils.decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    private static void saveToFilePublic(Context context, String fileName, PublicKey pub) throws Exception {
//        File root = android.os.Environment.getExternalStorageDirectory();
//        File directory = new File (root.getAbsolutePath() + "/Keys");
//        directory.mkdirs();
//        File file = new File(directory, fileName);
//        file.createNewFile();
        File file = new File(context.getFilesDir(), "Keys");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            File gpxfile = new File(file, fileName);
            ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(gpxfile)));
            try {
                oout.writeObject(pub);
            } catch (Exception e) {
                Log.d("SAVEFILE", e.toString());
            } finally {
                oout.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveToFilePrivate(Context context, String fileName, PrivateKey priv) throws Exception {
        File file = new File(context.getFilesDir(), "Keys");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            File gpxfile = new File(file, fileName);
            ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(gpxfile)));
            try {
                oout.writeObject(priv);
            } catch (Exception e) {
                Log.d("SAVEFILE", e.toString());
            } finally {
                oout.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PublicKey readPublicKey(Context context) throws Exception {
//        File root = android.os.Environment.getExternalStorageDirectory();
//        InputStream in = new FileInputStream(root.getAbsolutePath()+ "/Keys/" +PUBLIC_KEY_FILE);
//        ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
        File file = new File(context.getFilesDir(), "Keys");
        File gpxfile = new File(file, PUBLIC_KEY_FILE);
        InputStream in = new FileInputStream(gpxfile);
        ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
        PublicKey pubKey=null;
        try {
            pubKey = (PublicKey) oin.readObject();
            Log.d("aaaaaaaaaaaaaa",pubKey.toString());
            return pubKey;
        } catch (Exception e) {
            Log.d("READPUBKEY",e.toString());
        } finally {
            oin.close();
        }
        return pubKey;
    }

    public static PrivateKey readPrivatekey(Context context) throws Exception {
        File file = new File(context.getFilesDir(), "Keys");
        File gpxfile = new File(file, PRIVATE_KEY_FILE);
        InputStream in = new FileInputStream(gpxfile);
        ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));
        PrivateKey prikey=null;
        try {
            prikey = (PrivateKey) oin.readObject();
            return prikey;
        } catch (Exception e) {
            Log.d("READPRIVATEKEY",e.toString());
        } finally {
            oin.close();
        }
        return prikey;
    }
}
