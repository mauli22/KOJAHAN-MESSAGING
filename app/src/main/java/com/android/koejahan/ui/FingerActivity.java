package com.android.koejahan.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.StaticLayout;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.koejahan.R;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.data.StaticConfig;

import java.security.KeyStore;
import java.util.ArrayList;

import javax.crypto.Cipher;

public class FingerActivity extends AppCompatActivity {
    private FingerprintManager fm;
    private KeyguardManager keyguardManager;
    private TextView mParaLabel,tvuser;
    private ImageView fingerimage;

    private KeyStore keyStore;
    private Cipher cipher;
    private String KEY_NAME = "AndroidKey";

    public static int ACTION_START_CHAT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger);
        mParaLabel = findViewById(R.id.paraLabel);
        fingerimage = findViewById(R.id.id_finger);

        //getDatafromfriend
        Intent intentData = getIntent();
        StaticConfig.idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        StaticConfig.roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        StaticConfig.nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);
        StaticConfig.idfriend = intentData.getStringExtra(StaticConfig.INTENT_ID_FRIEND);
        StaticConfig.publikFriend = intentData.getStringExtra(StaticConfig.INTENT_PublikFriend);

        if (StaticConfig.finger_Session){
            Intent intent = new Intent(FingerActivity.this, ChatActivity.class);
            intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, StaticConfig.nameFriend);
            intent.putExtra(StaticConfig.INTENT_ID_FRIEND, StaticConfig.idfriend);
            intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, StaticConfig.idFriend);
            intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, StaticConfig.roomId);
            intent.putExtra(StaticConfig.INTENT_PublikFriend, StaticConfig.publikFriend);
            startActivity(intent);
        }

        Log.d("FINGER","hasil = "+StaticConfig.roomId+", "+StaticConfig.nameFriend);
        tvuser = findViewById(R.id.tvNamauser);
        tvuser.setText(SharedPreferenceHelper.getInstance(FingerActivity.this).getNAME());
        // Check 1: Android version should be greater or equal to Marshmallow
        // Check 2: Device has Fingerprint Scanner
        // Check 3: Have permission to use fingerprint scanner in the app
        // Check 4: Lock screen is secured with atleast 1 type of lock
        // Check 5: Atleast 1 Fingerprint is registered

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fm = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if (!fm.isHardwareDetected()) {
                mParaLabel.setText("Fingerprint Scanner not detected in Device");
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                mParaLabel.setText("Permission not granted to use Fingerprint Scanner");
            } else if (!keyguardManager.isKeyguardSecure()) {

                mParaLabel.setText("Add Lock to your Phone in Settings");

            } else if (!fm.hasEnrolledFingerprints()) {

                mParaLabel.setText("You should add atleast 1 Fingerprint to use this Feature");

            } else {

                mParaLabel.setText("Place your Finger on Scanner to Access the App.");
                FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                fingerprintHandler.startAuth(fm,null);
                fingerprintHandler.startAuth(fm, cryptoObject);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
        private Context context;
        private Handler handler = new Handler();

        public FingerprintHandler(Context context) {
            this.context = context;
        }

        public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){
            CancellationSignal cancellationSignal = new CancellationSignal();
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            this.update("There was an Auth Error. " + errString, false);
        }

        @Override
        public void onAuthenticationFailed() {

            this.update("Auth Failed. ", false);

        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

            this.update("Error: " + helpString, false);

        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

            this.update("You can now access the app.", true);

        }


        private void update(String s, boolean b) {
            TextView paraLabel = (TextView) ((Activity)context).findViewById(R.id.paraLabel);
            ImageView imageView = (ImageView) ((Activity)context).findViewById(R.id.id_finger);

            paraLabel.setText(s);

            if(b == false){

                paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

            } else {

                paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorSucces));
                imageView.setImageResource(R.drawable.ceklist);
                Toast.makeText(FingerprintHandler.this.context,"Tunggu beberapa detik untuk mendekripsi pesan !",Toast.LENGTH_SHORT).show();

                // set niali fingerprint true
                handler.postDelayed(runnable,1500);

            }

        }

        public Runnable runnable = new Runnable() {
            @Override
            public void run() {
                StaticConfig.finger_Session = true;
                Intent intent = new Intent(FingerprintHandler.this.context, ChatActivity.class);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, StaticConfig.nameFriend);
                intent.putExtra(StaticConfig.INTENT_ID_FRIEND, StaticConfig.idfriend);
                intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, StaticConfig.idFriend);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, StaticConfig.roomId);
                intent.putExtra(StaticConfig.INTENT_PublikFriend, StaticConfig.publikFriend);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d("FINGERHANDLER","NILAI = "+StaticConfig.roomId+" , "+StaticConfig.nameFriend);
                FingerprintHandler.this.context.startActivity(intent);
            }
        };
    }
}
