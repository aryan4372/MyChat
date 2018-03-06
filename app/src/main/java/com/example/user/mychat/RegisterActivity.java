package com.example.user.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText mName;
    private EditText mEmail;
    private EditText mPass;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ProgressDialog mRegProgress;
    private DatabaseReference mDatabase;
    private DatabaseReference mToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName=(EditText)findViewById(R.id.mName);
        mEmail=(EditText)findViewById(R.id.mEmail);
        mPass=(EditText)findViewById(R.id.mPass);
        mAuth=FirebaseAuth.getInstance();
        mToken=FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("CREATE ACCOUNT");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRegProgress=new ProgressDialog(this);
    }

    public void submit(View v)
    {
        String name=mName.getText().toString();
        String email=mEmail.getText().toString();
        String password= mPass.getText().toString();
        mRegProgress.setTitle("Registering User");
        mRegProgress.setMessage("Please wait while we create your account");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();
        register(name,email,password);
    }

    private void register(final String name,String email,String password)
    {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser cur_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=cur_user.getUid();
                    mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    HashMap<String,String> userMap=new HashMap<String, String>();
                    userMap.put("name",name);
                    userMap.put("status","I Am Using Mychat");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                String cur_id=mAuth.getCurrentUser().getUid();
                                String token= FirebaseInstanceId.getInstance().getToken();
                                mToken.child(cur_id).child("device_token").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mRegProgress.dismiss();
                                        Intent mainIntent =new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
                else
                {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"error",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
