package com.example.user.mychat;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by USER on 18-02-2018.
 */

public class MyChat extends Application {

    private DatabaseReference mDatabase;
    private String mCurrentUser;
    private FirebaseAuth mAuth;

    public void onCreate()
    {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mAuth=FirebaseAuth.getInstance();
        final FirebaseUser CurrentUser=mAuth.getCurrentUser();
        if(CurrentUser!=null)
        {
            mCurrentUser=CurrentUser.getUid();
        }

        if(CurrentUser!=null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser);
        }

        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        if(CurrentUser!=null) {
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        if (CurrentUser != null) {
                            mDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
