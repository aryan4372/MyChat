package com.example.user.mychat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mUserId;
    private String mUserName;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase;
    private DatabaseReference mRoot;
    private TextView mTitle;
    private TextView mLastSeen;
    private CircleImageView mImage;
    private FirebaseAuth mAuth;
    private ImageButton mAdd;
    private EditText mMessage;
    private ImageButton mSend;
    private String current_user;
    private StorageReference mImageStorage;

    private static final int GALLERY_PICK=1;
    private RecyclerView mMessageList;
    private final List<Messages> messagesList=new ArrayList();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private DatabaseReference mNotifications;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAdd=(ImageButton)findViewById(R.id.mAdd);
        mMessage=(EditText)findViewById(R.id.mMessage);
        mSend=(ImageButton)findViewById(R.id.mSend);
        mRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        mImageStorage= FirebaseStorage.getInstance().getReference();

        mNotifications=FirebaseDatabase.getInstance().getReference().child("notifications");

        mToolbar=(Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDatabase= FirebaseDatabase.getInstance().getReference();
        mUserId=getIntent().getStringExtra("user_id");
        mUserName=getIntent().getStringExtra("user_name");
        mAuth=FirebaseAuth.getInstance();
        mRoot=FirebaseDatabase.getInstance().getReference();

        current_user=mAuth.getCurrentUser().getUid();

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        mTitle=(TextView)findViewById(R.id.mTitle);
        mLastSeen=(TextView)findViewById(R.id.mLastSeen);
        mImage=(CircleImageView)findViewById(R.id.mImage);

        mDatabase.child("Users").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online=dataSnapshot.child("online").getValue().toString();
                if(online.equals("online")) {
                    mLastSeen.setText(online);
                }
                else
                {
                    GetTimeAgo g=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String last=g.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeen.setText(last);
                }
                String userImage=dataSnapshot.child("thumb_image").getValue().toString();
                Picasso.with(ChatActivity.this).load(userImage).placeholder(R.drawable.images).into(mImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mTitle.setText(mUserName);

        mRoot.child("Chat").child(current_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mUserId))
                {
                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen","false");
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("Chat/"+current_user+"/"+mUserId,chatAddMap);
                    chatUserMap.put("Chat/"+mUserId+"/"+current_user,chatAddMap);

                    mRoot.updateChildren(chatUserMap,new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null)
                            {
                                Toast.makeText(ChatActivity.this,"ERROR",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAdapter=new MessageAdapter(messagesList);
        mMessageList=(RecyclerView)findViewById(R.id.chat_screen);
        mLinearLayout=new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);
        loadMessages();

    }


    public void onStart()
    {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        String mCurrentUser=currentUser.getUid();
        mDatabase.child("Users").child(mCurrentUser).child("online").setValue("online");

    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        String mCurrentUser = currentUser.getUid();
        mDatabase.child("Users").child(mCurrentUser).child("online").setValue(ServerValue.TIMESTAMP);
    }

    public void send(View v)
    {
        String message=mMessage.getText().toString();
        mMessage.setText("");
        if(message.length()>0)
        {
            String sender="Messages/"+current_user+"/"+mUserId;
            String receiver="Messages/"+mUserId+"/"+current_user;
            DatabaseReference user_message_push=mRoot.child("Messages").child(current_user).child(mUserId).push();
            String push_id=user_message_push.getKey();
            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen","false");
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",current_user);

            Map messageUserMap=new HashMap();
            messageUserMap.put(sender+"/"+push_id,messageMap);
            messageUserMap.put(receiver+"/"+push_id,messageMap);

            mRoot.updateChildren(messageUserMap,new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            HashMap<String,String> notification=new HashMap<String, String>();
                            notification.put("from",current_user);
                            notification.put("type","message");
                            mNotifications.child(mUserId).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                            if(databaseError!=null)
                            {
                                Toast.makeText(ChatActivity.this,"ERROR",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
            );



        }
    }

    private void loadMessages()
    {
        mRoot.child("Messages").child(current_user).child(mUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message=dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);

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
    }

    public void add(View v)
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK)
        {
            Uri imageUri=data.getData();
            final  String current_user_ref="Messages/"+current_user+"/"+mUserId;
            final String chat_user_ref="Messages/"+mUserId+"/"+current_user;

            DatabaseReference user_message_push=mRoot.child("Messages").child(current_user).child(mUserId).push();
            final String push_id=user_message_push.getKey();

            StorageReference filePath=mImageStorage.child("message_images").child(push_id+".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        String download_url=task.getResult().getDownloadUrl().toString();
                        Map messageMap=new HashMap();
                        messageMap.put("message",download_url);
                        messageMap.put("seen","false");
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",current_user);

                        Map messageUsermap=new HashMap();
                        messageUsermap.put(current_user_ref+"/"+push_id,messageMap);
                        messageUsermap.put(chat_user_ref+"/"+push_id,messageMap);

                        mMessage.setText("");
                        mRoot.updateChildren(messageUsermap,new DatabaseReference.CompletionListener()
                        {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                HashMap<String,String> notification=new HashMap<String, String>();
                                notification.put("from",current_user);
                                notification.put("type","message");
                                mNotifications.child(mUserId).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });

                                if(databaseError!=null)
                                {
                                    Toast.makeText(ChatActivity.this,"ERROR",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        }

    }
}
