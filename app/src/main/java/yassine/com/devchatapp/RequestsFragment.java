package yassine.com.devchatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private RecyclerView myRequestsList;
    private View myMainView;
    private DatabaseReference friendsRequestsReference;
    private FirebaseAuth mAuth;
    String online_user_id;
    private DatabaseReference usersreference;
    private DatabaseReference FriendsDatabaseRef;
    private DatabaseReference FriendsReqDatabaseRef;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView = inflater.inflate(R.layout.fragment_requests, container, false);
        myRequestsList = (RecyclerView) myMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        friendsRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);

        usersreference = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReqDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        myRequestsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myRequestsList.setLayoutManager(linearLayoutManager);



        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests,RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(Requests.class,R.layout.friends_request_all_users_layout,RequestsFragment.RequestViewHolder.class, friendsRequestsReference) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Requests model, int position) {
                final String list_user_id = getRef(position).getKey();
                DatabaseReference get_type_ref = getRef(position).child("request_type").getRef();
                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String request_type = dataSnapshot.getValue().toString();
                            if(request_type.equals("received")){
                                usersreference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String image = dataSnapshot.child("user_image").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_status").getValue().toString();
                                        viewHolder.setUserName(userName);
                                        viewHolder.setImage(image);
                                        viewHolder.setStatus(userStatus);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Accept Friend Request", "Cancel Friend Request"
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Options");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {
                                                        if(position==0){
                                                            Calendar callForDate = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                            final String saveCurrentDate = currentDate.format(callForDate.getTime());
                                                            FriendsDatabaseRef.child(online_user_id).child(list_user_id).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    FriendsDatabaseRef.child(list_user_id).child(online_user_id).child("date").setValue(saveCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            FriendsReqDatabaseRef.child(online_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        FriendsReqDatabaseRef.child(list_user_id).child(online_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                        {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                            {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(),"Friend Request Accepted",Toast.LENGTH_SHORT).show();

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
                                                        if(position==1){
                                                            FriendsReqDatabaseRef.child(online_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        FriendsReqDatabaseRef.child(list_user_id).child(online_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(),"Friend Request Cancel",Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }


                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if(request_type.equals("sent")){
                                Button req_sent_btn = viewHolder.mView.findViewById(R.id.request_acc_btn);
                                req_sent_btn.setText("Req Sent");

                                viewHolder.mView.findViewById(R.id.request_dec_btn).setVisibility(View.INVISIBLE);


                                usersreference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final String image = dataSnapshot.child("user_image").getValue().toString();
                                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus = dataSnapshot.child("user_status").getValue().toString();
                                        viewHolder.setUserName(userName);
                                        viewHolder.setImage(image);
                                        viewHolder.setStatus(userStatus);
                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                CharSequence options[] = new CharSequence[]{
                                                        "Cancel Friend Request",
                                                };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Request Sent");
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {

                                                        if(position==0){
                                                            FriendsReqDatabaseRef.child(online_user_id).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        FriendsReqDatabaseRef.child(list_user_id).child(online_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    Toast.makeText(getContext(),"Friend Request Cancel",Toast.LENGTH_SHORT).show();
                                                                                }

                                                                            }
                                                                        });
                                                                    }


                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };
        myRequestsList.setAdapter(firebaseRecyclerAdapter);
    }








    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUserName(String userName) {
            TextView userNameDisplay = (TextView)mView.findViewById(R.id.request_profile_name);
            userNameDisplay.setText(userName);
        }

        public void setImage(String image)
        {
            CircleImageView image_t = (CircleImageView) mView.findViewById(R.id.request_profile_image);
            Picasso.get().load(image).placeholder(R.drawable.default_profile).into(image_t);

        }

        public void setStatus(String userStatus) {
            TextView user_status = (TextView)mView.findViewById(R.id.request_profile_status);
            user_status.setText(userStatus);

        }
    }

}
