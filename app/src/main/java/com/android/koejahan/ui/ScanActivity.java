package com.android.koejahan.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AndroidException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.koejahan.CryptoLib.RSA;
import com.android.koejahan.MainActivity;
import com.android.koejahan.R;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.FileDescriptor;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import static android.app.PendingIntent.getActivity;

public class ScanActivity extends AppCompatActivity {

    private TextView file,judul;
    private Uri targetFile;
    private String filename;
    private static final int READ_REQUEST_CODE = 42;
    private String tagPhone = "Mauli-PHONE-Success";
    private String value;
    private String kodeAsli="";
    private Button getfile,scan,task;
    private Dialog myDialog;
    private LovelyProgressDialog waitingDialog;
    private Handler handler = new Handler();
    private long mBackPressed;
    private static final int TIME_INTERVAL = 2000;
    private TextView txtclose,oke;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        getfile = findViewById(R.id.button);
        scan = findViewById(R.id.readQRcode);
        judul = findViewById(R.id.tvjudlScan);
        file = findViewById(R.id.EdStringdir);
        waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
        myDialog = new Dialog(ScanActivity.this);
        myDialog.setContentView(R.layout.activity_splash);
        txtclose = myDialog.findViewById(R.id.txtclose);
        oke = myDialog.findViewById(R.id.oke);

        scan.setVisibility(View.GONE);
    }

    public void doRead(View view) {
        try {
            scanQRImage(getBitmapFromUri(targetFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getfile(View view) {
        new change().cancel(true);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);

        scan.setVisibility(View.VISIBLE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            targetFile = null;
            if (resultData != null) {
                targetFile = resultData.getData();
                Log.d("GETGAMBAR", "Uri: " + targetFile.toString());
                dumpImageMetaData(targetFile);
                //showImage(uri);
            }
        }
    }

    public void dumpImageMetaData(Uri uri) {
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("GETNAMEFILE", "Display Name: " + filename);
                file.setText(filename);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("GETSIZEFILE", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public String scanQRImage(Bitmap bMap) {
        waitingDialog.setIcon(R.drawable.ic_person_low)
                .setTitle("Autentikasi QR Code, Harap tunggu !....")
                .setTopColorRes(R.color.grey_800)
                .setCancelable(true)
                .show();
        String nomortelepon = SharedPreferenceHelper.getInstance(ScanActivity.this).getPhone();
        String contents = null;
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
            final User userInfo = new User();
            final String pembacaan = contents;
            final PublicKey pubkey = RSA.readPublicKey(ScanActivity.this);
            FirebaseDatabase.getInstance().getReference().child("user/" ).orderByChild("phone").equalTo(nomortelepon).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                    String secVal = (String) mapMessage.get("secretVal");
                    try {
                        if (RSA.verify(secVal, pembacaan,pubkey))
                        //if (pembacaan.equals("1615101234"))
                        {
                            waitingDialog.dismiss();
                            userInfo.name = (String) mapMessage.get("name");
                            userInfo.email = (String) mapMessage.get("email");
                            userInfo.avata = (String) mapMessage.get("avata");
                            userInfo.id = dataSnapshot.getKey();
                            SharedPreferenceHelper.getInstance(ScanActivity.this).saveUserInfo(userInfo);
                            SharedPreferenceHelper.getInstance(ScanActivity.this).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_LOGIN,true);
                            Toast.makeText(ScanActivity.this,"Welcome "+userInfo.name,Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else{
                            waitingDialog.dismiss();
                            Toast.makeText(ScanActivity.this,"QR Code anda mungkin sudah usang !",Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        waitingDialog.dismiss();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    waitingDialog.dismiss();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    waitingDialog.dismiss();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    waitingDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    waitingDialog.dismiss();
                }
            });
        }
        catch (Exception e) {
            waitingDialog.dismiss();
            Log.d("QrTest", "Error decoding barcode", e);
        }
        return contents;
    }

    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            txtclose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDialog.dismiss();
                }
            });
            oke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new change().execute("");
                    finish();
                    myDialog.dismiss();
                }
            });
            myDialog.show();
        } else {
            Toast.makeText(getBaseContext(), "Click two times to close an activity",    Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }


    private class change extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            for (int i = 0; i < 600; i++) {
                if (isCancelled()) {
                    break;
                }
                try {
                    Thread.sleep(1000); //1 detik
                    if (i==599){
                        SharedPreferenceHelper.getInstance(ScanActivity.this).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_DAPETQR,false);
                    }
                } catch (InterruptedException e) {
                    // We were cancelled; stop sleeping!
                }
            }
            return "Executed";
        }

        @Override
        protected void onCancelled(){
            SharedPreferenceHelper.getInstance(ScanActivity.this).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_DAPETQR,true);
            Log.d("anjink","cancel cok");
        }
    }

}
