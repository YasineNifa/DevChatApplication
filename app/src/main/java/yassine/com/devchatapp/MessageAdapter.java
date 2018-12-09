package yassine.com.devchatapp;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by Mohammed on 01/12/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabasereference;
    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;

    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_users, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(V);

    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        String message_sender_id = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();
        UsersDatabasereference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersDatabasereference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("user_image")){
                    String receiverImage = dataSnapshot.child("user_image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.default_profile).into(holder.receiverProfileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text")){
            holder.receivermessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.sendermessageText.setVisibility(View.INVISIBLE);

            if(message_sender_id.equals(fromUserId)){
                holder.sendermessageText.setVisibility(View.VISIBLE);
                holder.sendermessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.sendermessageText.setTextColor(Color.BLACK);
                holder.sendermessageText.setText(messages.getMessage());
            }

            else{
                holder.sendermessageText.setVisibility(View.INVISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receivermessageText.setVisibility(View.VISIBLE);

                holder.receivermessageText.setBackgroundResource(R.drawable.receiver_msg_layout);
                holder.receivermessageText.setTextColor(Color.BLACK);
                holder.receivermessageText.setText(messages.getMessage());

            }
        }
        else{
            holder.receivermessageText.setVisibility(View.INVISIBLE);
            holder.receiverProfileImage.setVisibility(View.INVISIBLE);
            holder.sendermessageText.setVisibility(View.INVISIBLE);
            holder.messagePicture.setVisibility(View.INVISIBLE);

            if(message_sender_id.equals(fromUserId)){
                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.messagePicture.setVisibility(View.VISIBLE);
                holder.messagePicture.setPadding(0,0,0,0);
                holder.sendermessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(holder.messagePicture);
                //holder.messagePicture.setBackgroundResource(R.drawable.message_text_background_t);

            }
            else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messagePicture.setVisibility(View.VISIBLE);
                //holder.receiverProfileImage.setBackground(R.drawable.receiver_msg_layout);
                holder.messagePicture.setPadding(0,0,0,0);
                holder.receivermessageText.setBackgroundResource(R.drawable.receiver_msg_layout);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(holder.messagePicture);


                //holder.messagePicture.setBackgroundResource(R.drawable.message_text_background);

            }
            holder.sendermessageText.setText(messages.getMessage());


        }

    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView sendermessageText,receivermessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messagePicture;

        public MessageViewHolder(View view){
            super(view);
            sendermessageText = (TextView) view.findViewById(R.id.sender_message_text);
            receivermessageText= (TextView) view.findViewById(R.id.receiver_message_text);
            messagePicture = (ImageView) view.findViewById(R.id.message_image);
            receiverProfileImage = (CircleImageView)view.findViewById(R.id.message_profile_image);
        }
    }
}
