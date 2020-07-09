package com.android.koejahan.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.koejahan.CryptoLib.RSA;
import com.android.koejahan.MainActivity;
import com.android.koejahan.R;
import com.android.koejahan.data.ProsesLogin;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.data.StaticConfig;
import com.android.koejahan.model.User;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    private String kodeAsli = "" ;
    private String otpAsli = "";
    private String phone="";
    private EditText otp;
    private TextView timer;
    private String tagPhone = "Mauli-PHONE-Success";
    private String tagOtp = "Mauli-OTP-Success";
    private FirebaseAuth mAuth;
    private String valRandom="";
    private PublicKey pubkey;
    private PrivateKey privkey;
    private LovelyProgressDialog waitingDialog;
    private ConstraintLayout cly;
    private String value = "" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        otp = findViewById(R.id.editTextOTP);
        timer = findViewById(R.id.tv_timer);
        cly = findViewById(R.id.verifyOTPLayout);
        mAuth = FirebaseAuth.getInstance();
        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
        phone = SharedPreferenceHelper.getInstance(OTPActivity.this).getPhone();
        StringBuilder j = new StringBuilder();
        value = doGenerate(j);
        waitingDialog.setIcon(R.drawable.ic_person_low)
                .setTitle("Mengirim OTP ke nomor teleponmu....")
                .setTopColorRes(R.color.grey_800)
                .show();
        try{
            pubkey = RSA.readPublicKey(OTPActivity.this);
            privkey = RSA.readPrivatekey(OTPActivity.this);
        }catch(Exception e){
            waitingDialog.dismiss();
            Log.d("EROR_OTP","READ PUB DAN PRIV GAGAL");
        }
        sendVerificationCode(phone); //send otp automaticly
        kirimulang();
    }

    //method countime
    private void kirimulang(){
        new CountDownTimer(60000,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                View child = cly.getViewById(R.id.buttonVerify2);
                child.setVisibility(View.GONE);
                timer.setText("Kirim Ulang SMS : "+millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                timer.setText("Kirim Ulang SMS : 0");
                View child = cly.getViewById(R.id.buttonVerify2);
                View child2 = cly.getViewById(R.id.tv_timer);
                child2.setVisibility(View.GONE);
                child.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    //method to send sms verification
    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
        waitingDialog.dismiss();

    }

    //the callback to detect the verification status
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String kodeAsli2 = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (kodeAsli2 != null) {
                otp.setText(kodeAsli2);
                waitingDialog.setIcon(R.drawable.ic_person_low)
                        .setTitle("Mengirim QR Code ke email....")
                        .setTopColorRes(R.color.grey_800)
                        .show();
                //verifying the code
                verifyVerificationCode(kodeAsli2);
            }else{
                otp.setText("gagal");
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            valRandom = s;
            Log.d("codesent", "onCodeSent:" + s);
            Toast.makeText(OTPActivity.this,"Kode terkirim",Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyVerificationCode(String code) {
        //creating the credential
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(valRandom, code);
            Log.d("RANDOMNUMBER","nilai random = "+valRandom);
            final User userInfo = new User();
            final String Sign_secval =  RSA.sign(value,privkey);
            FirebaseDatabase.getInstance().getReference().child("user/" ).orderByChild("phone").equalTo(phone).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                    userInfo.phone = (String) mapMessage.get("phone");
                    userInfo.email = (String) mapMessage.get("email");
                    userInfo.name = (String) mapMessage.get("name");
                    userInfo.avata = (String) mapMessage.get("avata");
                    userInfo.id = dataSnapshot.getKey();
                    userInfo.bio = (String) mapMessage.get("bio");
                    byte[] byte_pubkey = pubkey.getEncoded();
                    userInfo.publik_key = Base64.encodeToString(byte_pubkey,Base64.DEFAULT);
                    userInfo.secretVal = value;

                    //update database masukkan nilai pubkey dan secret value.
                    FirebaseDatabase.getInstance().getReference().child("user/" + userInfo.id).setValue(userInfo);

//                    SharedPreferenceHelper.getInstance(OTPActivity.this).saveUserInfo(userInfo);
//                    SharedPreferenceHelper.getInstance(OTPActivity.this).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_LOGIN,true);

                    Toast.makeText(OTPActivity.this,"Kode otp yang diterima = ",Toast.LENGTH_SHORT).show();

                    //kirim secret value ke api untuk dikirim ke email pengguna dan generate qr code
                    sendEmailQR(phone,userInfo.email,Sign_secval);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            Toast.makeText(this,"Kode yang kamu masukkan salah !",Toast.LENGTH_SHORT).show();
            waitingDialog.dismiss();
        }
        //signing the user
        //signInWithPhoneAuthCredential(credential);
    }

    public void cekOtp(View view) {
        String getotp = otp.getText().toString();
        try {
            waitingDialog.setIcon(R.drawable.ic_person_low)
                    .setTitle("Mengirim QR Code ke Email....")
                    .setTopColorRes(R.color.grey_800)
                    .setCancelable(true)
                    .show();

            //input manual kode bosQ
            verifyVerificationCode(getotp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendGain(View view) {
        sendVerificationCode(phone);
        View child2 = cly.getViewById(R.id.tv_timer);
        child2.setVisibility(View.VISIBLE);
        kirimulang();
        Toast.makeText(this,"trying...",Toast.LENGTH_SHORT).show();
    }
    public boolean sendEmailQR(final String phone, final String email, final String signcrypt){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, StaticConfig.SendEmail_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server
                        if (response.contains(StaticConfig.responsePublickeySend)) {
                            waitingDialog.dismiss();
                            SharedPreferenceHelper.getInstance(OTPActivity.this).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_DAPETQR,true);
                            Toast.makeText(OTPActivity.this,"Kode diterima ",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(OTPActivity.this, ScanActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            waitingDialog.dismiss();
                            Toast.makeText(OTPActivity.this,"Cek koneksi atau server diluar jangkauan !",Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        waitingDialog.dismiss();
                        Toast.makeText(OTPActivity.this,"Error volley",Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put(StaticConfig.KEY_PHONE,phone);
                params.put(StaticConfig.KEY_EMAIL,email);
                params.put(StaticConfig.KEY_Enkripsi_SecretVal,signcrypt);

                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        return true;
    }
    public static String doGenerate(StringBuilder sb){
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        sb = new StringBuilder(9);
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

}



