package com.example.user.mychat;

import android.content.Context;
import android.drm.DrmStore;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
 * Created by USER on 26-02-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public MessageAdapter(List<Messages> mMessageList)
    {
        this.mMessageList=mMessageList;
    }

    public MessageViewHolder onCreateViewHolder(ViewGroup parent,int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView messageText;
        public TextView userName;
        public CircleImageView userImage;
        public ImageView mImageView;


        public MessageViewHolder(View v)
        {
            super(v);
            messageText=(TextView)v.findViewById(R.id.message_text);
            userName=(TextView)v.findViewById(R.id.chat_user_name);
            userImage=(CircleImageView)v.findViewById(R.id.chat_user_image);
            mImageView=(ImageView)v.findViewById(R.id.image);
        }
    }

    public void onBindViewHolder(final MessageViewHolder viewHolder, int i)
    {
        Messages c=mMessageList.get(i);
        mAuth=FirebaseAuth.getInstance();
        final String current_user=mAuth.getCurrentUser().getUid();
        final String from=c.getFrom();
        final String type=c.getType();
        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(from).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image=dataSnapshot.child("thumb_image").getValue().toString();
                if(from.equals(current_user))
                {
                    viewHolder.userName.setText("You");
                }
                else {
                    String name=dataSnapshot.child("name").getValue().toString();
                    String []str=name.split(" ",2);
                    String first_name=str[0];
                    viewHolder.userName.setText(first_name);
                }
                Picasso.with(viewHolder.userImage.getContext()).load(image).placeholder(R.drawable.images).into(viewHolder.userImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(type.equals("text"))
        {
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.mImageView.setVisibility(View.INVISIBLE);
        }
        else if(type.equals("image"))
        {
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.mImageView.setVisibility(View.VISIBLE);
            Picasso.with(viewHolder.mImageView.getContext()).load(c.getMessage()).placeholder(R.drawable.images).into(viewHolder.mImageView);
        }
    }

    public int getItemCount()
    {
        return mMessageList.size();
    }
}
