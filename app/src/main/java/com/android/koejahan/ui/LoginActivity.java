package com.android.koejahan.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.koejahan.CryptoLib.RSA;
import com.android.koejahan.data.ProsesLogin;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.android.koejahan.MainActivity;
import com.android.koejahan.R;
import com.android.koejahan.data.StaticConfig;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class LoginActivity extends AppCompatActivity {
    private static String TAG = "LoginActivity";
    FloatingActionButton fab;
    private final Pattern VALID_phone = Pattern.compile("^[0-9]{9,13}$", Pattern.CASE_INSENSITIVE);
    private EditText editTextPhone;
    private LovelyProgressDialog waitingDialog;
    private static final String PUBLIC_KEY_FILE = "FilePublicKey";
    private static final String PRIVATE_KEY_FILE = "FilePrivateKey";
    private String tagOtp = "Mauli-OTP-Success";
    private String tagPhone = "Mauli-PHONE-Success";
    //private AuthUtils authUtils;
    private FirebaseAuth mAuth;

    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        editTextPhone = (EditText) findViewById(R.id.phone_number);
        mAuth = FirebaseAuth.getInstance();

        if (SharedPreferenceHelper.getInstance(this).cekSessionLogin()){
            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }else if (SharedPreferenceHelper.getInstance(this).cekQRcode()){
            startActivity(new Intent(LoginActivity.this, ScanActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }

    }



    public void clickRegisterLayout(View view) {
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this, fab, fab.getTransitionName());
            startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER, options.toBundle());
        } else {
            startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER);
        }
    }

    public void clickLogin(View view) {
        final String Phone = "+62"+editTextPhone.getText().toString();
        final ProsesLogin login = new ProsesLogin();
        //String password = editTextPassword.getText().toString();
        if (validate(editTextPhone.getText().toString())) {
            //authUtils.signIn(username, password);
            waitingDialog.setIcon(R.drawable.ic_person_low)
                    .setTitle("Memeriksa Nomor Telpon....")
                    .setTopColorRes(R.color.colorPrimary)
                    .setCancelable(true)
                    .show();
            FirebaseDatabase.getInstance().getReference().child("user/" ).orderByChild("phone").equalTo(Phone).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    userid = dataSnapshot.getKey();
                    if (!userid.equals(null)){
                        try {
                            File root = new File(LoginActivity.this.getFilesDir(), "Keys");//android.os.Environment.getExternalStorageDirectory();
                            File filePub =  new File(root, PUBLIC_KEY_FILE);//new File(root.getAbsolutePath()+ "/Keys/" +PUBLIC_KEY_FILE);
                            File filePriv = new File(root, PRIVATE_KEY_FILE);//new File(root.getAbsolutePath()+ "/Keys/" +PRIVATE_KEY_FILE);
                            if (!filePub.exists() && !filePriv.exists()){
                                RSA.generateKeyPair(LoginActivity.this);
                                //Log.d("GENERATING","SUUUUUUUUUUUKSES");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        waitingDialog.dismiss();
                        SharedPreferenceHelper.getInstance(LoginActivity.this).savePhone(Phone);
                        Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                        intent.putExtra(tagPhone,Phone);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else{
                        waitingDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Nomor Telepon tidak terdaftar, silahkan regristrasi terlebih dahulu !", Toast.LENGTH_SHORT).show();
                    }
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

        } else {
            editTextPhone.setError("Masukkan Nomor Handphone dengan benar !");
            Toast.makeText(this, "Nomor handphone", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED, null);
        finish();
    }

    public boolean validate(String phoneNumber) {
        Matcher matcher = VALID_phone.matcher(phoneNumber);
        return (phoneNumber.length() > 0 && !phoneNumber.startsWith("08") && matcher.find());
    }
}
