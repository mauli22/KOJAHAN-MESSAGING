package com.android.koejahan.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.koejahan.CryptoLib.AES;
import com.android.koejahan.CryptoLib.HASH;
import com.android.koejahan.CryptoLib.RSA;
import com.android.koejahan.MainActivity;
import com.android.koejahan.notification.Data;
import com.android.koejahan.notification.Sender;
import com.android.koejahan.notification.Token;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.android.koejahan.R;
import com.android.koejahan.data.SharedPreferenceHelper;
import com.android.koejahan.data.StaticConfig;
import com.android.koejahan.model.Consersation;
import com.android.koejahan.model.Message;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    public static ListMessageAdapter adapter;
    private String roomId,idSelectedFriend,avatafriend,nameFriend;
    private ArrayList<CharSequence> idFriend;
    private Consersation consersation;
    private ImageButton btnSend;
    private EmojiconEditText editWriteMessage;
    private ImageView emojiImageView;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;
    private SecretKey kuncisimetris;
    private View rootView;
    private EmojIconActions emojIcon;
    private DatabaseReference refrence;
    private boolean notify = false;
    private byte[] iv;
    private PublicKey pubkey, pubkeyFriend;
    private PrivateKey privkey;
    private String time,today,tmp_publikFriend;
    private boolean isonline;
    ProgressDialog pd;
    private DatabaseReference database;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferenceHelper.getInstance(this).cekDark()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
        setContentView(R.layout.activity_chat);
        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);
        idSelectedFriend = intentData.getStringExtra(StaticConfig.INTENT_ID_FRIEND);
        avatafriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_AVATA_FRIEND);
        tmp_publikFriend = intentData.getStringExtra(StaticConfig.INTENT_PublikFriend);
        emojiImageView = findViewById(R.id.emoji_btn);
        rootView = findViewById(R.id.rootview);

        try{
            pubkey = RSA.readPublicKey(ChatActivity.this);
            privkey = RSA.readPrivatekey(ChatActivity.this);
        }catch(Exception e){

        }

        //-----------------SharedPreferenceHelper.getInstance(this).setFirstTimeLaunch(true);

        consersation = new Consersation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        String base64AvataUser = SharedPreferenceHelper.getInstance(this).getUserInfo().avata;
        if (!base64AvataUser.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            bitmapAvataUser = null;
        }

        editWriteMessage = findViewById(R.id.editWriteMessage);

        emojIcon = new EmojIconActions(this, rootView, editWriteMessage, emojiImageView);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("TAGOpen", "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("TAGClose", "Keyboard closed");
            }
        });

        //count time 1
        long starttime = System.currentTimeMillis();

        if (idFriend != null && nameFriend != null) {
            checkLastSeen();
            //ngegate id sender untuk membedakan keyShared atau enc_key
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(this, consersation, bitmapAvataFriend, bitmapAvataUser);
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        Message newMessage = new Message();
                        newMessage.idPesan = dataSnapshot.getKey();
                        newMessage.idroom = roomId;
                        newMessage.idSender = (String) mapMessage.get("idSender");
                        newMessage.idReceiver = (String) mapMessage.get("idReceiver");
                        newMessage.isseen = (Boolean) mapMessage.get("isseen");
                        newMessage.tmp_paramIV = Base64.decode(String.valueOf(mapMessage.get("paramIV")),Base64.DEFAULT);
                        try {
                            //ngegate id sender untuk membedakan keyShared atau enc_key
                            if (newMessage.idSender.equals(SharedPreferenceHelper.getInstance(ChatActivity.this).getUID())){
                                newMessage.tmp_key = RSA.decrypt(String.valueOf(mapMessage.get("enc_key")),privkey); //kunci shared di dekripsi dulu baru masuk ke aeskey
                                Log.d("CHATAPP","THIS = "+newMessage.tmp_key);
                                newMessage.AESkey = Base64.decode(newMessage.tmp_key,Base64.DEFAULT);
                                newMessage.tmp_text = Base64.decode(String.valueOf(mapMessage.get("text")),Base64.DEFAULT);
                                newMessage.skey = new SecretKeySpec(newMessage.AESkey, 0, newMessage.AESkey.length, "AES" );
                                Log.d("Pesan enkripsi","text diluar if "+newMessage.tmp_text);
                                newMessage.text = AES.AESDecryption(newMessage.tmp_text,newMessage.skey,newMessage.tmp_paramIV);
                                newMessage.auth = (String) mapMessage.get("auth");
                                //newMessage.text = (String) mapMessage.get("text");
                                newMessage.timestamp = (long) mapMessage.get("timestamp");
                                consersation.getListMessageData().add(newMessage);
                                adapter.notifyDataSetChanged();
                                linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);
                            }else{
                                newMessage.keyShared = RSA.decrypt(String.valueOf(mapMessage.get("keyShared")),privkey); //kunci shared di dekripsi dulu baru masuk ke aeskey
                                Log.d("CHATAPP","THIS = "+newMessage.keyShared);
                                newMessage.AESkey = Base64.decode(newMessage.keyShared,Base64.DEFAULT);
                                newMessage.tmp_text = Base64.decode(String.valueOf(mapMessage.get("text")),Base64.DEFAULT);
                                newMessage.skey = new SecretKeySpec(newMessage.AESkey, 0, newMessage.AESkey.length, "AES" );
                                Log.d("Pesan enkripsi","text diluar if "+newMessage.tmp_text);
                                newMessage.text = AES.AESDecryption(newMessage.tmp_text,newMessage.skey,newMessage.tmp_paramIV);
                                //newMessage.text = (String) mapMessage.get("text");
                                newMessage.timestamp = (long) mapMessage.get("timestamp");
                                //get auth pesan dari si teman
                                newMessage.auth = (String) mapMessage.get("auth");
                                byte[] byte_pubkeyFriend = Base64.decode(tmp_publikFriend,Base64.DEFAULT); //pubkey teman ini
                                pubkeyFriend = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(byte_pubkeyFriend)); //pubkey teman ini
                                if (otentikPesan(newMessage.auth,pubkeyFriend,newMessage.text)){ //authentikasi pesan
                                    consersation.getListMessageData().add(newMessage);
                                    adapter.notifyDataSetChanged();
                                    linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);
                                }else{
                                    Toast.makeText(ChatActivity.this, "MITM attack Detected !", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            recyclerChat.setAdapter(adapter);
        }

        seenMessage();
        updateToken(FirebaseInstanceId.getInstance().getToken());

        iv = AES.createInitializationVector();
        //count time 2
        long end = System.currentTimeMillis();
        long time = end - starttime;
        Toast.makeText(this, "Total waktu "+time+" milisecond", Toast.LENGTH_SHORT).show();
    }

    private void checkLastSeen(){
        DatabaseReference friend = FirebaseDatabase.getInstance().getReference("user/"+idSelectedFriend);
        DatabaseReference stats = friend.child("status");
        stats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    long waktu = dataSnapshot.child("timestamp").getValue(long.class);
                    isonline = dataSnapshot.child("isOnline").getValue(boolean.class);
                    time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(waktu));
                    today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));

                    ActionBar aa = getSupportActionBar();
                    aa.setDisplayHomeAsUpEnabled(true);
//                    aa.setDisplayShowHomeEnabled(true);
//                    aa.setLogo(R.drawable.kojahan);
//                    aa.setDisplayUseLogoEnabled(true);

                    aa.setTitle(nameFriend);
                    if (isonline){
                        aa.setSubtitle("Online");
                    }else{
                        if (today.equals(time)){
                            aa.setSubtitle("Last seen at "+ new SimpleDateFormat("HH:mm").format(new Date(waktu)));
                        }else{
                            aa.setSubtitle("Last seen at "+ new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(waktu)));
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private boolean otentikPesan(String DigitalSign, PublicKey publik, String pesan){
        try {
            String bongkar = RSA.decryptWithPublic(DigitalSign,publik);
            if (bongkar.equals(HASH.SHA512(pesan))){
                Log.d("DIGITAL SIGN","yo BRO!");
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendNotification(final String receiver, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens/"+receiver);
        //Query query = tokens.orderByKey().equalTo(receiver);
        tokens.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    Token token = dataSnapshot.getValue(Token.class);
                    Log.d("NILAI Token","nilainya = "+dataSnapshot.child("token").getValue(String.class));
                    Data data = new Data(SharedPreferenceHelper.getInstance(ChatActivity.this).getUID(), R.drawable.ic_chat, message, "Kojahan Messaging",receiver);

                    Sender sender = new Sender(data, token.getToken());

                    //fcm json object
                    try {
                        JSONObject senderJsonobj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonobj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        //respone request
                                        Log.d("JSON_RESPONSE","Resp = "+response.toString());
                                    }
                                }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("JSON_RESPONSE","Resp = "+error.toString());
                                }
                        }
                        ){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                //put param
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-type","application/json");
                                headers.put("Authorization","key=AAAAUHnw1TQ:APA91bE6namK-N_fDg2DoHVHvkNFx4PyTR1VPRFLhUR-k64mv-rKgQpNU8Y6Dqk354VctrgnaIIG7i7GHMmh8GzAjrdXvyYDDek5WPhILzGVg-1xEgXezPydg_4rlnYOsMTLEn5yeVSl");

                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(){
        FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null){
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message.idSender.equals(null)){
                        Log.d("CHAT","yoasu");
                    }
                    else if (message.idSender.equals(idSelectedFriend)){
                        HashMap<String, Object> hashmap = new HashMap<>();
                        hashmap.put("isseen", true);
                        dataSnapshot.getRef().updateChildren(hashmap);
                        adapter.notifyDataSetChanged();
                    }
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
    }

    private void updateToken(String token){
        DatabaseReference refrence = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        refrence.child(SharedPreferenceHelper.getInstance(ChatActivity.this).getUID()).setValue(token1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent result = new Intent(this, MainActivity.class);
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            result.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(result);
            this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent(this, MainActivity.class);
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        result.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(result);
        this.finish();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            notify = true;
            Log.d("KLIKsend","NILAI NOTIFY = "+notify);
            String content = editWriteMessage.getText().toString().trim();
            try {
                kuncisimetris = AES.buatkunci();
                byte[] kunci = kuncisimetris.getEncoded();
                byte[] enkrip = AES.AESEncryption(content,kuncisimetris,iv);
                String E_content = Base64.encodeToString(enkrip, Base64.DEFAULT);
                if (content.length() > 0) {
                    String hashPesan = HASH.SHA512(content); //hash pesan
                    byte[] byte_pubkeyFriend = Base64.decode(tmp_publikFriend,Base64.DEFAULT); //pubkey teman ini
                    pubkeyFriend = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(byte_pubkeyFriend)); //pubkey teman ini
                    Log.d("CHAT SEND","nilai pubkey = "+pubkeyFriend.toString());
                    editWriteMessage.setText("");
                    Message newMessage = new Message();
                    newMessage.text = E_content; //text enkripsi
                    newMessage.idSender = StaticConfig.UID;
                    newMessage.idReceiver = roomId;
                    newMessage.paramIV = Base64.encodeToString(iv,Base64.DEFAULT);
                    newMessage.timestamp = System.currentTimeMillis();
                    newMessage.isseen = false;
                    newMessage.enc_key = RSA.encrypt(Base64.encodeToString(kunci,Base64.DEFAULT),pubkey); //key enkripsi
                    newMessage.keyShared = RSA.encrypt(Base64.encodeToString(kunci,Base64.DEFAULT),pubkeyFriend);
                    newMessage.auth = RSA.encryptWithPrivate(hashPesan,privkey);
                    Log.d("CHATAPP","THIS = "+Base64.encodeToString(kunci,Base64.DEFAULT));

                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                    String msg = SharedPreferenceHelper.getInstance(ChatActivity.this).getNAME()+" : "+E_content;
                    if (notify){
                        sendNotification(idSelectedFriend, msg);
                        Log.d("NOTIFY CHATTTT","notifysEND");
                    }
                    notify = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Consersation consersation;
    private HashMap<String, Bitmap> bitmapAvata;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private Bitmap bitmapAvataUser;



    public ListMessageAdapter(Context context, Consersation consersation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser) {
        this.context = context;
        this.consersation = consersation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        bitmapAvataDB = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageFriendHolder) {
            ((ItemMessageFriendHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
            Bitmap currentAvata = bitmapAvata.get(consersation.getListMessageData().get(position).idSender);
            if (currentAvata != null) {
                ((ItemMessageFriendHolder) holder).avata.setImageBitmap(currentAvata);
            } else {
                final String id = consersation.getListMessageData().get(position).idSender;
                if(bitmapAvataDB.get(id) == null){
                    bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avata"));
                    bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if(!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                    byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                }else{
                                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            if (bitmapAvataUser != null) {
                ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
                if (consersation.getListMessageData().get(position).isseen == true){
                    ((ItemMessageUserHolder) holder).seen.setImageResource(R.drawable.centang2);
                }else{
                    ((ItemMessageUserHolder) holder).seen.setImageResource(R.drawable.check1);
                }
            }

            ((ItemMessageUserHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
            if (consersation.getListMessageData().get(position).isseen == true){
                ((ItemMessageUserHolder) holder).seen.setImageResource(R.drawable.centang2);

            }else{
                ((ItemMessageUserHolder) holder).seen.setImageResource(R.drawable.check1);

            }
            ((ItemMessageUserHolder) holder).setOnItemLongClickListener(new ItemMessageUserHolder.onClickListner() {
                @Override
                public void onItemLongClick(final int position, View v) {
                    Log.d("CEK",String.valueOf(position));
                    final String authPesan = consersation.getListMessageData().get(position).auth;
                    final String idroom = consersation.getListMessageData().get(position).idroom;
                    final String idpesan = consersation.getListMessageData().get(position).idPesan;
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Chat")
                            .setMessage("Yakin ingin menghapus Chat dari obrolan ?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    final ProgressDialog pd = new ProgressDialog(context);
                                    pd.setMessage("Menghapus Chat...");
                                    pd.setCancelable(true);
                                    pd.show();
                                    deleteMessage(authPesan,idroom,idpesan);
                                    pd.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                    //Toast.makeText(context,consersation.getListMessageData().get(position).auth,Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void deleteMessage(final String authMessage, final String roomid, final String idpesan) {
        //Log.d("PENASARAN","isinya"+roomid+", "+idpesan);
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("message/"+roomid).child(idpesan).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                database.child("message/"+roomid).child(idpesan).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"success delete", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(context, MainActivity.class);
                        ((Activity)context).finish();
                        notifyDataSetChanged();
                        context.startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return consersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return consersation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
    private static onClickListner onclicklistner;
    public EmojiconTextView txtContent;
    public CircleImageView avata;
    public ImageView seen;
    private View headerView;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
        seen = itemView.findViewById(R.id.tvseen);
        itemView.setOnLongClickListener(this);

    }

    @Override
    public boolean onLongClick(View v) {
        onclicklistner.onItemLongClick(getAdapterPosition(), v);
        return true;
    }

    public void setOnItemLongClickListener(onClickListner onclicklistner) {
        ItemMessageUserHolder.onclicklistner = onclicklistner;
    }

    public interface onClickListner {
        void onItemLongClick(int position, View v);
    }

}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public EmojiconTextView txtContent;
    public CircleImageView avata;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
    }
}
