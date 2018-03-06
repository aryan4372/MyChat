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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private RecyclerView mChatList;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mChatDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public ChatFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatList = (RecyclerView) mMainView.findViewById(R.id.chat_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatDatabase=FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    public void onStart()
    {
        super.onStart();
        Query conversationQuery=mChatDatabase.orderByChild("timestamp");
        FirebaseRecyclerAdapter<Friends,ChatFragment.ChatViewHolder> ChatRecyclerViewAdapter =new FirebaseRecyclerAdapter<Friends,ChatViewHolder>(
                Friends.class,
                R.layout.users_layout,
                ChatFragment.ChatViewHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ChatFragment.ChatViewHolder viewHolder, Friends model, int position) {
                final String user_id=getRef(position).getKey();

                Query lastMessageQuery=mDatabase.child(user_id).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data=dataSnapshot.child("message").getValue().toString();
                        String type=dataSnapshot.child("type").getValue().toString();
                        if(type.equals("text")) {
                            viewHolder.setStatus(data);
                        }
                        else
                        {
                            viewHolder.setStatus("image");
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

                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName=dataSnapshot.child("name").getValue().toString();
                        String userImage=dataSnapshot.child("thumb_image").getValue().toString();
                        String status=dataSnapshot.child("online").getValue().toString();
                        viewHolder.setName(userName);
                        viewHolder.setImage(userImage,getContext());
                        viewHolder.setOnlineStatus(status);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener()
                        {
                            public void onClick(View v)
                            {
                                Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("user_id",user_id);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mChatList.setAdapter(ChatRecyclerViewAdapter);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public ChatViewHolder(View view) {
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
            if(status.equals("online"))
            {
                ImageView img=(ImageView)mView.findViewById(R.id.onlineStatus);
                img.setVisibility(View.VISIBLE);
            }
            else
            {
                ImageView img=(ImageView)mView.findViewById(R.id.onlineStatus);
                img.setVisibility(View.INVISIBLE);
            }
        }
    }

}
