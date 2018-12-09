package yassine.com.devchatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private Button SendFriendRequestButton;
    private Button DeclineFriendrequestButton;
    private TextView ProfileName;
    private TextView ProfileStatus;
    private ImageView ProfileImage;
    private DatabaseReference UsersReference;
    private String Current_state;
    private DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;
    String sender_user_id;
    String receiver_user_id;
    private DatabaseReference FriendsReference;
    private DatabaseReference NotificationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);
        NotificationsReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsReference.keepSynced(true);

        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        SendFriendRequestButton = (Button)findViewById(R.id.profile_visit_send_request_button);
        DeclineFriendrequestButton = (Button)findViewById(R.id.profile_decline_request_button);
        DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendrequestButton.setEnabled(false);
        ProfileName = (TextView) findViewById(R.id.profile_visit_username);
        ProfileStatus = (TextView)findViewById(R.id.profile_visit_status);
        ProfileImage = (ImageView)findViewById(R.id.profile_visit_user_image);

        Current_state = "not_friends";
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);
        UsersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(ProfileImage);
                FriendRequestReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiver_user_id)){
                            String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();
                            if(req_type.equals("sent")){
                                Current_state = "request_sent";
                                SendFriendRequestButton.setText("Cancel Friend Req");
                                DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendrequestButton.setEnabled(false);
                            }
                            else if(req_type.equals("received"))
                            {
                                Current_state = "request_received";
                                SendFriendRequestButton.setText("Accept Friend Request");

                                DeclineFriendrequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendrequestButton.setEnabled(true);
                                DeclineFriendrequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DeclineFriendRequest();
                                    }
                                });

                            }
                        }


                        else{
                            FriendsReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiver_user_id)){
                                        Current_state = "friends";
                                        SendFriendRequestButton.setText("Unfriend");
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {


                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        if(!sender_user_id.equals(receiver_user_id)){
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SendFriendRequestButton.setEnabled(false);
                    if (Current_state.equals("not_friends"))
                    {
                        DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                        DeclineFriendrequestButton.setEnabled(false);
                        SendFriendRequestButtonToFriend();

                    }
                    if(Current_state.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(Current_state.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(Current_state.equals("friends")){
                        UnFriendaFriend();
                    }
                }
            });

        }
        else{
            DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }
    }
    private void DeclineFriendRequest()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendRequestButton.setEnabled(true);
                                Current_state = "not_friends";
                                SendFriendRequestButton.setText("Add Friend");
                                DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendrequestButton.setEnabled(false);
                            }

                        }
                    });
                }


            }
        });
    }

    private void UnFriendaFriend()
    {
        FriendsReference.child(sender_user_id).child(receiver_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendFriendRequestButton.setEnabled(true);
                                Current_state= "not_friends";
                                SendFriendRequestButton.setText("Add Friend");
                                DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendrequestButton.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptFriendRequest()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate = currentDate.format(callForDate.getTime());
        FriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                FriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendRequestButton.setEnabled(true);
                                                Current_state = "friends";
                                                SendFriendRequestButton.setText("Unfriend");

                                                DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendrequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                                }


                            }
                        });
                    }
                });
            }
        });
    }

    private void CancelFriendRequest()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendFriendRequestButton.setEnabled(true);
                                Current_state = "not_friends";
                                SendFriendRequestButton.setText("Add Friend");
                                DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendrequestButton.setEnabled(false);
                            }

                        }
                    });
                }


            }
        });
    }

    private void SendFriendRequestButtonToFriend()
    {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    FriendRequestReference.child(receiver_user_id).child(sender_user_id).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                HashMap<String, String> notificationsData = new HashMap<>();
                                notificationsData.put("from",sender_user_id);
                                notificationsData.put("type", "request");

                                NotificationsReference.child(receiver_user_id).push().setValue(notificationsData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            SendFriendRequestButton.setEnabled(true);
                                            Current_state = "request_sent";
                                            SendFriendRequestButton.setText("Cancel Friend Req");
                                            DeclineFriendrequestButton.setVisibility(View.INVISIBLE);
                                            DeclineFriendrequestButton.setEnabled(false);
                                        }

                                    }
                                });

                            }
                        }
                    });

                }
            }
        });

    }
}
