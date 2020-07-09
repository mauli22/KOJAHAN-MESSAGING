package com.android.koejahan.data;


import java.util.ArrayList;

public class StaticConfig {
    //localhost to send Email
    public static final String url = "http://labpluto.club/";
    public static final String SendEmail_URL = url+"kojahanAPI/entryUser/send_Email.php";
    public static final String responsePublickeySend = "Pubkey terkirim" ;
    public static final String responseOTPsend = "otpsukses" ;
    public static final String KEY_EMAIL= "email_user";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_OTP = "otp";
    public static final String KEY_HASH = "CRNaXMqp";
    public static final String KEY_Enkripsi_SecretVal = "en_secval";

    public static boolean finger_Session = false ;
    public static int REQUEST_CODE_REGISTER = 2000;
    public static String STR_EXTRA_ACTION_LOGIN = "login";
    public static String STR_EXTRA_ACTION_RESET = "resetpass";
    public static String STR_EXTRA_ACTION = "action";
    public static String STR_EXTRA_USERNAME = "username";
    //public static String STR_EXTRA_PASSWORD = "password";
    public static String STR_EXTRA_PHONE = "phone";
    public static String STR_DEFAULT_BASE64 = "default";
    public static String UID = "";
    public static ArrayList<CharSequence> idFriend ;
    public static String roomId = "";
    public static String nameFriend = "";
    public static String idfriend = "";
    public static String publikFriend = "";
    //TODO only use this UID for debug mode
//    public static String UID = "6kU0SbJPF5QJKZTfvW1BqKolrx22";
    public static String INTENT_KEY_CHAT_FRIEND = "friendname";
    public static String INTENT_KEY_AVATA_FRIEND = "friendavata";
    public static String INTENT_KEY_CHAT_ID = "friendid";
    public static String INTENT_KEY_CHAT_ROOM_ID = "roomid";
    public static String INTENT_ID_FRIEND = "selectedFriendID";
    public static String INTENT_PublikFriend = "PublicKeyFriend";
    public static long TIME_TO_REFRESH = 10 * 1000;
    public static long TIME_TO_OFFLINE = 1 * 20 * 1000;

}
