package yassine.com.devchatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentGroupName;
    private String currentUserId, currentUserName, currentDate, currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameref, GroupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this,currentGroupName, Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameref = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        mToolbar = (Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        SendMessageButton = (ImageButton)findViewById(R.id.send_msg_button);
        userMessageInput = (EditText)findViewById(R.id.input_group_msg);
        displayTextMessages = (TextView)findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView)findViewById(R.id.my_scroll_view);

        GetUserInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
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

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while(iterator.hasNext()){
            String chatDate =(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage =(String)((DataSnapshot)iterator.next()).getValue();
            String chatName =(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime =(String)((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + ":\n" + chatMessage + "\n" + chatTime + " "+ chatDate + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void SaveMessageInfoDatabase() {
        String message = userMessageInput.getText().toString();
        String messagekey = GroupNameref.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"Please write a message",Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar forDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM dd, yyyy");
            currentDate = currentDateFormat.format(forDate.getTime());

            Calendar forTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
            currentTime = currentTimeFormat.format(forTime.getTime());

            HashMap<String,Object> groupMessageKey = new HashMap<>();
            GroupNameref.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameref.child(messagekey);
            HashMap<String,Object>MessageInfoMap = new HashMap<>();
            MessageInfoMap.put("name", currentUserName);
            MessageInfoMap.put("message", message);
            MessageInfoMap.put("date", currentDate);
            MessageInfoMap.put("time", currentTime);
            GroupMessageKeyRef.updateChildren(MessageInfoMap);


        }

    }

    private void GetUserInfo() {
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("user_name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
