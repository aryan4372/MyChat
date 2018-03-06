package com.example.user.mychat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    private TextInputLayout mStatus;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);


        mAuth=FirebaseAuth.getInstance();
        mDatabase1=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mToolbar=(Toolbar)findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("CHANGE STATUS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String curr_uid=mCurrentUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(curr_uid);

        mStatus=(TextInputLayout)findViewById(R.id.new_status);
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

    public void save(View v)
    {
        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Saving Changes");
        mProgress.setMessage("Please Wait While We Save The Changes");
        mProgress.show();

        String status=mStatus.getEditText().getText().toString();
        mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    mProgress.dismiss();
                }
                else
                {
                    mProgress.hide();
                    Toast.makeText(StatusActivity.this,"error",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
