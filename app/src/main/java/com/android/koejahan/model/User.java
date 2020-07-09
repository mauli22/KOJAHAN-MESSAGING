package com.android.koejahan.model;



public class User {
    public String id;
    public String name;
    public String phone;
    public String email;
    public String avata;
    public Status status;
    public Message message;
    public String secretVal;
    public String publik_key;
    public String bio;


    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
