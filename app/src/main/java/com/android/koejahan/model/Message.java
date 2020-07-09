package com.android.koejahan.model;


import javax.crypto.SecretKey;

public class Message{
    public String idSender;
    public String idReceiver;
    public String text;
    public byte[] tmp_text;
    public String tmp_key;
    public byte[] AESkey;
    public byte[] tmp_paramIV;
    public String paramIV;
    public long timestamp;
    public String enc_key;
    public String keyShared;
    public String auth;
    public SecretKey skey;
    public boolean isseen;
    public String idroom;
    public String idPesan;
}