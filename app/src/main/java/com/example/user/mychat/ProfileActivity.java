package com.example.user.mychat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ViewUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseFriend;
    private DatabaseReference mFriends;
    private DatabaseReference mNotifications;

    private FirebaseUser mCurrentuser;
    private int mCurrentState=0;
    private String visited_user;
    private Button mButton;
    private Button mSecondButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        mDatabase1=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        String user_id=getIntent().getStringExtra("user_id");
        visited_user=user_id;
        mButton=(Button)findViewById(R.id.mButton);
        mSecondButton=(Button)findViewById(R.id.second_button);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mDatabaseFriend=FirebaseDatabase.getInstance().getReference().child("friend_request");
        mFriends=FirebaseDatabase.getInstance().getReference().child("friends");
        mCurrentuser= FirebaseAuth.getInstance().getCurrentUser();
        mNotifications=FirebaseDatabase.getInstance().getReference().child("notifications");

        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_name);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);

        if(visited_user.equals(mCurrentuser.getUid().toString()))
        {
            mButton.setText("Delete Profile");
            mSecondButton.setEnabled(false);
            mSecondButton.setVisibility(View.INVISIBLE);
            mCurrentState=4;

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    mProfileName.setText(name);
                    mProfileStatus.setText(status);
                    Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.images).into(mProfileImage);
                }

                public void onCancelled(DatabaseError databaseError) {

                }
        });
        }
        else
        {
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();

                    mProfileName.setText(name);
                    mProfileStatus.setText(status);
                    Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.images).into(mProfileImage);

                    mDatabaseFriend.child(mCurrentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(visited_user)) {
                                String req_type = dataSnapshot.child(visited_user).child("request_type").getValue().toString();
                                if (req_type.equals("received")) {
                                    mCurrentState = 2;
                                    mButton.setText("Accept Friend Request");
                                    mSecondButton.setText("Decline Friend Request");
                                    mSecondButton.setVisibility(View.VISIBLE);
                                    mSecondButton.setEnabled(true);
                                } else if (req_type.equals("sent")) {
                                    mCurrentState = 1;
                                    mButton.setText("Cancel Friend Request");
                                    mSecondButton.setEnabled(false);
                                    mSecondButton.setVisibility(View.INVISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mFriends.child(mCurrentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(visited_user)) {
                                mButton.setText("UnFriend");
                                mCurrentState = 3;
                                mSecondButton.setEnabled(false);
                                mSecondButton.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void onStart()
    {
        super.onStart();
        mDatabase1.child("online").setValue("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabase1.child("online").setValue(ServerValue.TIMESTAMP);
        System.gc();
    }

    public void Send(View v)
    {
        if(mCurrentState==0)
        {
            mDatabaseFriend.child(mCurrentuser.getUid()).child(visited_user).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        mDatabaseFriend.child(visited_user).child(mCurrentuser.getUid()).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    HashMap<String,String> notification=new HashMap<String, String>();
                                    notification.put("from",mCurrentuser.getUid());
                                    notification.put("type","request");
                                    mNotifications.child(visited_user).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(ProfileActivity.this,"Request Sent",Toast.LENGTH_LONG).show();
                                            mButton.setText("Cancel Friend Request");
                                            mCurrentState=1;
                                            mSecondButton.setEnabled(false);
                                            mSecondButton.setVisibility(View.INVISIBLE);
                                        }
                                    });

                                }
                                else
                                {
                                    Toast.makeText(ProfileActivity.this,"Error",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this,"Error",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        else if(mCurrentState==1)
        {
            mDatabaseFriend.child(mCurrentuser.getUid()).child(visited_user).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mDatabaseFriend.child(visited_user).child(mCurrentuser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mCurrentState=0;
                            mButton.setText("Send Friend Request");
                            mSecondButton.setEnabled(false);
                            mSecondButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        }

        else if(mCurrentState==2)
        {
            final String currentDate= new Date().toString();
            mFriends.child(mCurrentuser.getUid()).child(visited_user).child("Date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFriends.child(visited_user).child(mCurrentuser.getUid()).child("Date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mDatabaseFriend.child(mCurrentuser.getUid()).child(visited_user).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabaseFriend.child(visited_user).child(mCurrentuser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                HashMap<String,String> notification=new HashMap<String, String>();
                                                notification.put("from",mCurrentuser.getUid());
                                                notification.put("type","accept");
                                                mNotifications.child(visited_user).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mCurrentState=3;
                                                        mButton.setText("UnFriend");
                                                    }
                                                });
                                            }
                                            else
                                            {
                                                Toast.makeText(ProfileActivity.this,"error",Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                }
                            });

                        }
                    });
                }
            });
        }

        else if(mCurrentState==3)
        {
            mFriends.child(mCurrentuser.getUid()).child(visited_user).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFriends.child(visited_user).child(mCurrentuser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mCurrentState=0;
                            mButton.setText("Send Friend Request");
                            mSecondButton.setEnabled(false);
                            mSecondButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        }
    }

    public void secondClick(View v)
    {
        mDatabaseFriend.child(mCurrentuser.getUid()).child(visited_user).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseFriend.child(visited_user).child(mCurrentuser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mCurrentState=0;
                        mButton.setText("Send Friend Request");
                        mSecondButton.setEnabled(false);
                        mSecondButton.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }
}
