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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ProgressDialog mRegProcess;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail=(EditText)findViewById(R.id.email);
        mPass=(EditText)findViewById(R.id.pass);
        mRegProcess=new ProgressDialog(this);
        mToolbar=(Toolbar)findViewById(R.id.page_login);
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login To MyChat");
        }

        public void login(View v)
        {
            String email=mEmail.getText().toString();
            String pass=mPass.getText().toString();
            mRegProcess.setTitle("Logging In");
            mRegProcess.setMessage("Please Wait,While We Log You In");
            mRegProcess.setCanceledOnTouchOutside(false);
            mRegProcess.show();
            login1(email,pass);
        }

        public void login1(String email,String pass)
        {
            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        String cur_id=mAuth.getCurrentUser().getUid();
                        String token= FirebaseInstanceId.getInstance().getToken();
                        mDatabase.child(cur_id).child("device_token").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mRegProcess.dismiss();
                                Intent in=new Intent(LoginActivity.this,MainActivity.class);
                                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(in);
                                finish();
                            }
                        });
                    }
                    else
                    {
                        mRegProcess.hide();
                        Toast.makeText(LoginActivity.this,"Credentials did not match",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

