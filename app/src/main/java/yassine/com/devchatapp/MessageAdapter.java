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
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {

        String message_sender_id = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        if(fromMessageType.equals("text")){
            holder.messagePicture.setVisibility(View.INVISIBLE);
            holder.messageText.setVisibility(View.VISIBLE);

            if(message_sender_id.equals(fromUserId)){
                //adding
                //lp2.gravity = Gravity.LEFT;
                //holder.messageText.setBackgroundResource(R.drawable.bubble_in);
                //adding
                holder.messageText.setBackgroundResource(R.drawable.message_text_background_t);
                holder.messageText.setTextColor(Color.BLACK);
                holder.messageText.setGravity(Gravity.LEFT);
            }
            else{
                //adding
                //lp2.gravity = Gravity.RIGHT;
                //holder.messageText.setBackgroundResource(R.drawable.bubble_out);
                //adding
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                holder.messageText.setTextColor(Color.WHITE);
                holder.messageText.setGravity(Gravity.RIGHT);
            }
            holder.messageText.setText(messages.getMessage());
            //holder.messageText.setLayoutParams(lp2);

        }
        else{
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.messagePicture.setVisibility(View.VISIBLE);

            if(message_sender_id.equals(fromUserId)){
                holder.messagePicture.setPadding(0,0,0,0);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(holder.messagePicture);
                //holder.messagePicture.setBackgroundResource(R.drawable.message_text_background_t);

            }
            else{
                holder.messagePicture.setPadding(0,0,0,0);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(holder.messagePicture);

                //holder.messagePicture.setBackgroundResource(R.drawable.message_text_background);

            }
            holder.messageText.setText(messages.getMessage());


        }


    }



    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView userProfileImage;
        public ImageView messagePicture;

        public MessageViewHolder(View view){
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_text);
            messagePicture = (ImageView) view.findViewById(R.id.message_image);
            userProfileImage = (CircleImageView)view.findViewById(R.id.message_profile_image);
        }
    }
}
