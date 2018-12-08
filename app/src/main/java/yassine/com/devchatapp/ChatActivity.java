package yassine.com.devchatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;
    private Toolbar ChatToolBar;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;
    private DatabaseReference rootRef;
    private ImageButton SendMessageButton;
    private ImageButton SelectImageButton;
    private EditText InputMessageText;
    private FirebaseAuth mAuth;
    private String messageSendId;
    private RecyclerView usermessageslist;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter messageAdapter;
    private static int Gallery_Pick = 1;
    private StorageReference MessageImageStorageRef;
    private ProgressDialog LoadingBar;
    //private  ImageButton imageProfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("user_name").toString();
        mAuth = FirebaseAuth.getInstance();
        messageSendId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        MessageImageStorageRef = FirebaseStorage.getInstance().getReference().child("Messages_Images");
        usermessageslist = (RecyclerView)findViewById(R.id.messages_list_users);

        Toast.makeText(ChatActivity.this,messageReceiverId,Toast.LENGTH_SHORT).show();
        Toast.makeText(ChatActivity.this,messageReceiverName,Toast.LENGTH_SHORT).show();

        ChatToolBar = (Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);
        LoadingBar = new ProgressDialog(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        userNameTitle = (TextView)findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView)findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = (CircleImageView)findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton)findViewById(R.id.send_message_btn);
        SelectImageButton = (ImageButton)findViewById(R.id.select_image);
        //imageProfil = (ImageView)findViewById(R.id.custom_profile_image);
        InputMessageText = (EditText)findViewById(R.id.input_msg);

        mLinearLayoutManager = new LinearLayoutManager(this);
        messageAdapter = new MessageAdapter(messageList);
        usermessageslist = (RecyclerView)findViewById(R.id.messages_list_users);
        usermessageslist.setHasFixedSize(true);
        usermessageslist.setLayoutManager(mLinearLayoutManager);
        usermessageslist.setAdapter(messageAdapter);
        userNameTitle.setText(messageReceiverName);
        FetchMessages();
        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userthumb = dataSnapshot.child("user_image").getValue().toString();
                //Picasso.get().load(userthumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(userChatProfileImage);


                Picasso.get().load(userthumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(userChatProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                    }
                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(userthumb).placeholder(R.drawable.default_profile).into(userChatProfileImage);

                    }
                });
                if(online.equals("true")){
                    userLastSeen.setText("online");
                }
                else{
                    LastSeenTime getTime = new LastSeenTime();
                    long last_seen = Long.parseLong(online);
                    String LastSeenTimeDisplay = getTime.getTimeAgo(last_seen, getApplicationContext()).toString();
                    userLastSeen.setText(LastSeenTimeDisplay);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
        SelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){

            LoadingBar.setTitle("Sending Image");
            LoadingBar.setMessage("Please wait while your message is sending");
            LoadingBar.show();
            Uri ImageUri = data.getData();
            final String message_sender_ref = "Messages/"+ messageSendId+"/"+messageReceiverId;
            final String message_receiver_ref = "Messages/"+ messageReceiverId+"/"+messageSendId;
            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSendId).child(messageReceiverId).push();
            final String message_push_id = user_message_key.getKey();

            StorageReference filePath = MessageImageStorageRef.child(message_push_id + ".jpg") ;
            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    //
                    if(task.isSuccessful()){
                        final String downloadUrl = task.getResult().getDownloadUrl().toString();

                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message",downloadUrl);
                        messageTextBody.put("seen",false);
                        messageTextBody.put("type","image");
                        messageTextBody.put("time", ServerValue.TIMESTAMP);
                        messageTextBody.put("from",messageSendId);
                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(message_sender_ref + "/"+ message_push_id, messageTextBody);
                        messageBodyDetails.put(message_receiver_ref + "/"+ message_push_id, messageTextBody);

                        rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    Log.d("Chat_Log", databaseError.getMessage().toString());
                                }
                                InputMessageText.setText("");
                                LoadingBar.dismiss();
                            }
                        });

                        Toast.makeText(ChatActivity.this, "Pictures was sent Successfully", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();

                    }
                    else{
                        Toast.makeText(ChatActivity.this, "Pictures was not sent", Toast.LENGTH_SHORT).show();
                        LoadingBar.dismiss();
                    }
                }
            });


        }
    }

    private void FetchMessages()
    {
        rootRef.child("Messages").child(messageSendId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messageList.add(messages);
                messageAdapter.notifyDataSetChanged();

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
    }

    private void SendMessage() {
        String messageText = InputMessageText.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this,"Please write your message",Toast.LENGTH_SHORT).show();

        }
        else{
            String message_sender_ref = "Messages/"+ messageSendId+"/"+messageReceiverId;
            String message_receiver_ref = "Messages/"+ messageReceiverId+"/"+messageSendId;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSendId).child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",messageSendId);
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/"+ message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/"+ message_push_id, messageTextBody);
            rootRef.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }
                    InputMessageText.setText("");
                }
            });



        }
    }


}
