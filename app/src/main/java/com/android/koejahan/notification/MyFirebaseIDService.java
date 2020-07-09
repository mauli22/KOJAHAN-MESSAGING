package com.android.koejahan.notification;

import android.content.Context;

import com.android.koejahan.data.SharedPreferenceHelper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIDService extends FirebaseInstanceIdService {

    private Context context;
    @Override
    public void onTokenRefresh(){
        super.onTokenRefresh();
        String id = SharedPreferenceHelper.getInstance(context).getUID();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (id != null){
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken) {
        DatabaseReference refrence = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshToken);
        refrence.child(SharedPreferenceHelper.getInstance(context).getUID()).setValue(token);
    }
}
