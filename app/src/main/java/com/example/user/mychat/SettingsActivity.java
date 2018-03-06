package com.example.user.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private DatabaseReference mDatabase;
    private FirebaseUser cur_user;
    String cur_id;

    private static final int GALLERY_PICK=1;
    private StorageReference mImageStorage;

    private DatabaseReference mDatabase1;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage=(CircleImageView)findViewById(R.id.display_image);
        mName=(TextView)findViewById(R.id.display_name);
        mStatus=(TextView)findViewById(R.id.display_status);

        mAuth=FirebaseAuth.getInstance();
        mDatabase1=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        cur_user= FirebaseAuth.getInstance().getCurrentUser();
        cur_id=cur_user.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(cur_id);
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default")) {
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.images).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.images).into(mDisplayImage);

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mImageStorage= FirebaseStorage.getInstance().getReference();
    }

    public void status(View v)
    {
        Intent in=new Intent(this,StatusActivity.class);
        startActivity(in);
        finish();
    }


    public void onStart()
    {
        super.onStart();
        mDatabase1.child("online").setValue("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        mDatabase1.child("online").setValue(ServerValue.TIMESTAMP);
    }

    public void image(View v)
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
            CropImage.activity(imageUri).setAspectRatio(1,1).start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            mProgressDialog=new ProgressDialog(SettingsActivity.this);
            mProgressDialog.setTitle("Uploading Image");
            mProgressDialog.setMessage("Please Wait While We Upload The Image");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                Uri resultUri=result.getUri();

                File thumb_filepath=new File(resultUri.getPath());
                try {
                    Bitmap thumb_bitmap=new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filepath);
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    final byte[] thumb_byte=baos.toByteArray();
                    final StorageReference thumb_storage=mImageStorage.child("profile_images").child("thumbs").child(cur_id+".jpg");

                    StorageReference filepath=mImageStorage.child("profile_images").child(cur_id+".jpg");
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                final String download_url=task.getResult().getDownloadUrl().toString();
                                UploadTask uploadTask=thumb_storage.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                        String thumb_downloadUrl= thumb_task.getResult().getDownloadUrl().toString();
                                        if(thumb_task.isSuccessful())
                                        {
                                            Map<String,Object> update_hashMap=new HashMap<>();
                                            update_hashMap.put("image",download_url);
                                            update_hashMap.put("thumb_image",thumb_downloadUrl);
                                            mDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        mProgressDialog.dismiss();
                                                    }
                                                    else
                                                    {
                                                        mProgressDialog.hide();
                                                        Toast.makeText(SettingsActivity.this,"Failed",Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
