package yassine.com.devchatapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View myMainView;
    private RecyclerView myChatsList;
    private DatabaseReference Friendsreference;
    private DatabaseReference UsersReference;
    private FirebaseAuth mAuth;
    String online_user_id;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView = inflater.inflate(R.layout.fragment_chats, container, false);
        myChatsList = (RecyclerView)myMainView.findViewById(R.id.chat_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        Friendsreference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        myChatsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);




        myChatsList.setLayoutManager(linearLayoutManager);
        return myMainView;
    }
    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Chats, ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder>(Chats.class, R.layout.all_users_display_layout, ChatsFragment.ChatsViewHolder.class, Friendsreference) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, int position) {
                //viewHolder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();
                UsersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String image = dataSnapshot.child("user_image").getValue().toString();


                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_status = (String)dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(online_status);
                        }
                        viewHolder.setUserName(userName);
                        viewHolder.setUserImage(image);
                        viewHolder.setUserStatus(userStatus);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(dataSnapshot.child("online").exists()){
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);
                                }
                                else{
                                    UsersReference.child(list_user_id).child("online").setValue(ServerValue.TIMESTAMP).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);

                                        }
                                    });

                                }
                            }
                        });
                        //FriendsViewHolder.setThumbImage(thumbImage, getContext());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myChatsList.setAdapter(firebaseRecyclerAdapter);
    }










    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }
        /*public void setDate(String date){
            TextView sinceFriendsDate = (TextView)mView.findViewById(R.id.all_users_status);
            sinceFriendsDate.setText(date);
        }*/
        public  void setUserName(String userName){
            TextView userNamaDisplay = (TextView)mView.findViewById(R.id.all_users_username);
            userNamaDisplay.setText(userName);
        }
        public void setUserImage(String user_image){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(user_image).placeholder(R.drawable.default_profile).into(image);
        }

        public void setThumbImage(final String thumbImage, Context ctx) {
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            //Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_profile).into(thumb_image);
            Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profile).into(thumb_image, new Callback() {
                @Override
                public void onSuccess() {


                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(thumbImage).placeholder(R.drawable.default_profile).into(thumb_image);

                }
            });
        }

        public void setUserOnline(String online_status) {
            ImageView onlineStatusView = (ImageView)mView.findViewById(R.id.online_status);
            if(online_status.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }
            else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus)
        {
            TextView user_status = (TextView)mView.findViewById(R.id.all_users_status);
            user_status.setText(userStatus);
        }
    }
}
