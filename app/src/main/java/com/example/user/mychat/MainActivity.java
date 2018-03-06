package com.example.user.mychat;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyChat");

        mViewPager=(ViewPager)findViewById(R.id.tab_pager);
        mSectionsPagerAdapter =new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void onStart()
    {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null)
        {
            Intent startIntent=new Intent(this,startActivity.class);
            startActivity(startIntent);
            finish();
        }
        else
        {
            String mCurrentUser=currentUser.getUid();
            mDatabase.child(mCurrentUser).child("online").setValue("online");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null) {
            String mCurrentUser = currentUser.getUid();
            mDatabase.child(mCurrentUser).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_btn)
        {
            FirebaseUser currentUser=mAuth.getCurrentUser();
            String mCurrentUser = currentUser.getUid();
            mDatabase.child(mCurrentUser).child("online").setValue("false");
            mAuth.signOut();
            Intent startIntent=new Intent(this,startActivity.class);
            startActivity(startIntent);
            finish();

        }

        if(item.getItemId()==R.id.account_settings)
        {
            Intent in=new Intent(this,SettingsActivity.class);
            startActivity(in);
        }

        if(item.getItemId()==R.id.all_users)
        {
            Intent in=new Intent(this,UsersActivity.class);
            startActivity(in);
        }
        return true;
    }
}
