package com.example.user.mychat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private RecyclerView mRequestList;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public RequestFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);
        mRequestList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mRequestList.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("friend_request").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerAdapter<Friends,RequestFragment.RequestViewHolder> RequestRecyclerViewAdapter =new FirebaseRecyclerAdapter<Friends,RequestFragment.RequestViewHolder>(
                Friends.class,
                R.layout.users_layout,
                RequestFragment.RequestViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestFragment.RequestViewHolder viewHolder, Friends model, int position) {

                final String user_id=getRef(position).getKey();
                mDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("request_type")) {
                            String requestType = dataSnapshot.child("request_type").getValue().toString();
                            if (requestType.equals("received")) {
                                mRequestList.setVisibility(View.VISIBLE);
                                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userName = dataSnapshot.child("name").getValue().toString();
                                        String userStatus = dataSnapshot.child("status").getValue().toString();
                                        String userImage = dataSnapshot.child("thumb_image").getValue().toString();
                                        String status = dataSnapshot.child("online").getValue().toString();
                                        viewHolder.setStatus(userStatus);
                                        viewHolder.setName(userName);
                                        viewHolder.setImage(userImage, getContext());
                                        viewHolder.setOnlineStatus(status);


                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                                profileIntent.putExtra("user_id", user_id);
                                                startActivity(profileIntent);
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
        mRequestList.setAdapter(RequestRecyclerViewAdapter);
    }



    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public RequestViewHolder(View view) {
            super(view);
            mView = view;
        }

        public void setStatus(String s)
        {
            TextView userName=(TextView)mView.findViewById(R.id.users_status);
            userName.setText(s);
        }


        public void setName(String name)
        {
            TextView userName=(TextView)mView.findViewById(R.id.users_name);
            userName.setText(name);
        }

        public void setImage(String thumb_image, Context ctx)
        {
            CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.users_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.images).into(userImageView);
        }

        public void setOnlineStatus(String status)
        {

                ImageView img=(ImageView)mView.findViewById(R.id.onlineStatus);
                img.setVisibility(View.INVISIBLE);
        }
    }


}
