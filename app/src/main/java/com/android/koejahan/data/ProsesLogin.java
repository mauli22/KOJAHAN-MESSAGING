package com.android.koejahan.data;

import android.content.Context;

import com.android.koejahan.model.User;
import com.android.koejahan.ui.LoginActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.HashMap;

public class ProsesLogin {
    private LovelyProgressDialog waitingDialog;
    private FirebaseUser user;
    /**
     * Simpan info pengguna untuk pengguna yang masuk
     */
    public void saveUserInfo(final Context context, String phone) {
        FirebaseDatabase.getInstance().getReference().child("user/").orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                User userInfo = new User();
                userInfo.name = (String) hashUser.get("name");
                userInfo.email = (String) hashUser.get("email");
                userInfo.avata = (String) hashUser.get("avata");
                userInfo.id = dataSnapshot.getKey();
                SharedPreferenceHelper.getInstance(context).saveUserInfo(userInfo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Saya membuka jendela info untuk akun baru
     */
    public boolean initNewUserInfo(String email, String phone) {
        String id = FirebaseDatabase.getInstance().getReference().child("user/").push().getKey();
        User newUser = new User();
        newUser.email = email;
        newUser.phone = phone;
        newUser.name = email.substring(0, email.indexOf("@"));
        newUser.avata = StaticConfig.STR_DEFAULT_BASE64;
        newUser.bio = "";
        FirebaseDatabase.getInstance().getReference().child("user/" + id).setValue(newUser);
        return true;
    }

    public void initSecvalUser (String secret, String id, String email, String phone, String name, String ava) {
        User newUser = new User();
        newUser.email = email;
        newUser.phone = phone;
        newUser.secretVal = secret;
        newUser.name = name;
        newUser.avata = ava;
        FirebaseDatabase.getInstance().getReference().child("user/" + id).setValue(newUser);
    }

    public void initUpdateUser (String secret, String id, String email, String phone, String name, String ava, String pubkey) {
        User newUser = new User();
        newUser.email = email;
        newUser.phone = phone;
        newUser.secretVal = secret;
        newUser.publik_key = pubkey;
        newUser.name = name;
        newUser.avata = ava;
        FirebaseDatabase.getInstance().getReference().child("user/" + id).setValue(newUser);
    }
}
