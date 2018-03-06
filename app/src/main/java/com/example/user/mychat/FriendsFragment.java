package com.example.user.mychat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;

    public FriendsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrent_user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter =new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_layout,
                FriendsViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String user_id=getRef(position).getKey();
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
                                CharSequence []options=new CharSequence[]{"Open Profile","Send Message"};
                                final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0)
                                        {
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",user_id);
                                            startActivity(profileIntent);
                                        }
                                        else if(which==1)
                                        {
                                            Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id",user_id);
                                            chatIntent.putExtra("user_name",userName);
                                            startActivity(chatIntent);
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
        };
        mFriendList.setAdapter(friendsRecyclerViewAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public FriendsViewHolder(View view)
        {
            super(view);
            mView=view;
        }

        public void setDate(String date)
        {
            TextView userName=(TextView)mView.findViewById(R.id.users_status);
            userName.setText(date);
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
