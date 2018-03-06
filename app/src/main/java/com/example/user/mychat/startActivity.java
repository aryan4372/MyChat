package com.example.user.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class startActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void register(View v)
    {
        Intent in=new Intent(this,RegisterActivity.class);
        startActivity(in);
    }

    public void login(View v)
    {
        Intent in=new Intent(this, LoginActivity.class);
        startActivity(in);
    }
}
