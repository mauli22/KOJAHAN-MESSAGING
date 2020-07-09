package com.android.koejahan.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.android.koejahan.R;
import com.android.koejahan.data.FriendDB;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.model.Configuration;
import com.android.koejahan.model.User;
import com.android.koejahan.service.ServiceUtils;
import com.android.koejahan.util.ImageUtils;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class UserProfileFragment extends Fragment {
    TextView tvUserName;
    ImageView avatar;

    private List<Configuration> listConfig = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserInfoAdapter infoAdapter;

    private static final String USERNAME_LABEL = "Username";
    private static final String EMAIL_LABEL = "Email";
    private static final String SIGNOUT_LABEL = "Sign out";
    private static final String BIO_LABEL = "Bio";

    private static final int PICK_IMAGE = 1994;
    private LovelyProgressDialog waitingDialog;

    private DatabaseReference userDB;
    private FirebaseAuth mAuth;
    private User myAccount;
    private Context context;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //Lấy thông tin của user về và cập nhật lên giao diện
            listConfig.clear();
            myAccount = dataSnapshot.getValue(User.class);
            Log.d("CEK PROFILE","Aaa "+myAccount.name+" , "+myAccount.email);

            setupArrayListInfo(myAccount);
            if(infoAdapter != null){
                infoAdapter.notifyDataSetChanged();
            }

            if(tvUserName != null){
                tvUserName.setText(myAccount.name);
            }

            setImageAvatar(context, myAccount.avata);
            myAccount.id = SharedPreferenceHelper.getInstance(context).getUID();
            SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(context);
            preferenceHelper.saveUserInfo(myAccount);
            setupArrayListInfo(myAccount);
            Log.d("USER PROFILE","preference execute");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            //Có lỗi xảy ra, không lấy đc dữ liệu
            Log.e(UserProfileFragment.class.getName(), "loadPost:onCancelled", databaseError.toException());
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (SharedPreferenceHelper.getInstance(getContext()).cekDark()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Log.d("Chat","darkmode");
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Log.d("Chat","lightmode");
        }
        userDB = FirebaseDatabase.getInstance().getReference().child("user").child(SharedPreferenceHelper.getInstance(context).getUID());
        userDB.addListenerForSingleValueEvent(userListener);
        mAuth = FirebaseAuth.getInstance();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        context = view.getContext();
        avatar = (ImageView) view.findViewById(R.id.img_avatar);
        avatar.setOnClickListener(onAvatarClick);
        tvUserName = (TextView)view.findViewById(R.id.tv_username);

        SharedPreferenceHelper prefHelper = SharedPreferenceHelper.getInstance(context);
        myAccount = prefHelper.getUserInfo();
        setupArrayListInfo(myAccount);
        setImageAvatar(context, myAccount.avata);
        tvUserName.setText(myAccount.name);

        recyclerView = (RecyclerView)view.findViewById(R.id.info_recycler_view);
        infoAdapter = new UserInfoAdapter(listConfig);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(infoAdapter);

        waitingDialog = new LovelyProgressDialog(context);
        return view;
    }


    private View.OnClickListener onAvatarClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            new AlertDialog.Builder(context)
                    .setTitle("Avatar")
                    .setMessage("Are you sure want to change avatar profile?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_PICK);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(context, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);
                myAccount.avata = imageBase64;

                waitingDialog.setCancelable(false)
                        .setTitle("Avatar updating....")
                        .setTopColorRes(R.color.colorPrimary)
                        .show();

                userDB.child("avata").setValue(imageBase64)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                    waitingDialog.dismiss();
                                    SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(context);
                                    preferenceHelper.saveUserInfo(myAccount);
                                    avatar.setImageDrawable(ImageUtils.roundedImage(context, liteImage));

                                    new LovelyInfoDialog(context)
                                            .setTopColorRes(R.color.colorPrimary)
                                            .setTitle("Success")
                                            .setMessage("Update avatar successfully!")
                                            .show();
                                }
                            }
                         })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                waitingDialog.dismiss();
                                Log.d("Update Avatar", "failed");
                                new LovelyInfoDialog(context)
                                        .setTopColorRes(R.color.colorAccent)
                                        .setTitle("False")
                                        .setMessage("False to update avatar")
                                        .show();
                            }
                        });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param myAccount
     */
    public void setupArrayListInfo(User myAccount){
        listConfig.clear();
        Configuration userNameConfig = new Configuration(USERNAME_LABEL, myAccount.name, R.mipmap.ic_account_box);
        listConfig.add(userNameConfig);

        Configuration emailConfig = new Configuration(EMAIL_LABEL, myAccount.email, R.mipmap.ic_email);
        listConfig.add(emailConfig);

        Configuration resetPass = new Configuration(BIO_LABEL, myAccount.bio, R.drawable.ic_bio);
        listConfig.add(resetPass);

        Configuration signout = new Configuration(SIGNOUT_LABEL, "", R.mipmap.ic_power_settings);
        listConfig.add(signout);
    }

    private void setImageAvatar(Context context, String imgBase64){
        try {
            Resources res = getResources();

            Bitmap src;
            if (imgBase64.equals("default")) {
                src = BitmapFactory.decodeResource(res, R.drawable.default_avata);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            avatar.setImageDrawable(ImageUtils.roundedImage(context, src));
        }catch (Exception e){
        }
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();
    }

    @Override
    public void onDestroy (){
        super.onDestroy();
    }

    public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ViewHolder>{
        private List<Configuration> profileConfig;

        public UserInfoAdapter(List<Configuration> profileConfig){
            this.profileConfig = profileConfig;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (SharedPreferenceHelper.getInstance(getContext()).cekDark()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Log.d("Chat","darkmode");
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Log.d("Chat","lightmode");
            }
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_info_item_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Configuration config = profileConfig.get(position);
            holder.label.setText(config.getLabel());
            holder.value.setText(config.getValue());
            holder.icon.setImageResource(config.getIcon());
            ((RelativeLayout)holder.label.getParent()).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(config.getLabel().equals(SIGNOUT_LABEL)){
                        FriendDB.getInstance(getContext()).dropDB();
                        ServiceUtils.stopServiceFriendChat(getContext().getApplicationContext(), true);
                        SharedPreferenceHelper.getInstance(context).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_LOGIN,false);
                        SharedPreferenceHelper.getInstance(context).saveSPBoolean(SharedPreferenceHelper.SP_SUDAH_DAPETQR, false);
                        Intent intent = new Intent(UserProfileFragment.this.context,LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }

                    if(config.getLabel().equals(USERNAME_LABEL)){
                        View vewInflater = LayoutInflater.from(context)
                                .inflate(R.layout.dialog_edit_username,  (ViewGroup) getView(), false);
                        final EditText input = (EditText)vewInflater.findViewById(R.id.edit_username);
                        input.setText(myAccount.name);

                        new AlertDialog.Builder(context)
                                .setTitle("Edit username")
                                .setView(vewInflater)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String newName = input.getText().toString();
                                        if(!myAccount.name.equals(newName)){
                                            changeUserName(newName);
                                        }
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }

                    if(config.getLabel().equals(BIO_LABEL)){
                        View vewInflater = LayoutInflater.from(context)
                                .inflate(R.layout.dialog_edit_bio,  (ViewGroup) getView(), false);
                        final EditText input2 = (EditText)vewInflater.findViewById(R.id.edit_bio);
                        input2.setText(myAccount.bio);

                        new AlertDialog.Builder(context)
                                .setTitle("Bio")
                                .setView(vewInflater)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        String newbio = input2.getText().toString();
                                        if(!myAccount.name.equals(newbio)){
                                            changeBio(newbio);
                                        }
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                }
            });
        }


        private void changeUserName(String newName){
            userDB.child("name").setValue(newName);


            myAccount.name = newName;
            SharedPreferenceHelper prefHelper = SharedPreferenceHelper.getInstance(context);
            prefHelper.saveUserInfo(myAccount);

            tvUserName.setText(newName);
            setupArrayListInfo(myAccount);
            notifyDataSetChanged();
        }

        private void changeBio(final String newBio) {
            userDB.child("bio").setValue(newBio);
            myAccount.bio = newBio;
            SharedPreferenceHelper xd = SharedPreferenceHelper.getInstance(context);
            xd.saveUserInfo(myAccount);
            setupArrayListInfo(myAccount);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return profileConfig.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView label, value;
            public ImageView icon;
            public ViewHolder(View view) {
                super(view);
                label = (TextView)view.findViewById(R.id.tv_title);
                value = (TextView)view.findViewById(R.id.tv_detail);
                icon = (ImageView)view.findViewById(R.id.img_icon);
            }
        }

    }

}
