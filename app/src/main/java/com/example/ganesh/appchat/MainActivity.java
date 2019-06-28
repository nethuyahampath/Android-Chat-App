package com.example.ganesh.appchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private String mCurrent_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");


        TabLayout tabLayout=(TabLayout)findViewById(R.id.main_tabs);
        ViewPager viewPager=(ViewPager)findViewById(R.id.main_view_pager);

        TabPageAdapter tabPageAdapter=new TabPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabPageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View navHeaderView=navigationView.getHeaderView(0);
        final ImageView navProfImage=(CircleImageView)navHeaderView.findViewById(R.id.nav_profile_image);
        final TextView navProfName=(TextView)navHeaderView.findViewById(R.id.nav_profile_name);
        final TextView navProEmail=(TextView)navHeaderView.findViewById(R.id.nav_profile_email);


        mAuth = FirebaseAuth.getInstance();

        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());



        if (mAuth.getCurrentUser() != null) {

            //sendToWelcome();
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());


            mUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){

                        String fullName=dataSnapshot.child("name").getValue().toString();
                        final String image=dataSnapshot.child("image").getValue().toString();
                        String email=dataSnapshot.child("email").getValue().toString();
                        String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                        navProfName.setText(fullName);
                        navProEmail.setText(email);


                        if(!image.equals("default")){

                            //Picasso.get().load(image).placeholder(R.drawable.user3).into(mImage);
                            Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.user).into(navProfImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(image).placeholder(R.drawable.user).into(navProfImage);

                                }
                            });


                        }



                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



            navProfImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingtIntent=new Intent(MainActivity.this,SettingActivity.class);
                    startActivity(settingtIntent);
                }
            });



        }



        //navigation drawer menu












    }

    //firebase
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

        if(currentUser==null){
            sendToWelcome();
        }else{
            mUserRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
           // mUserRef.child("last_seen").setValue(ServerValue.TIMESTAMP);
        }


    }

    private void sendToWelcome() {
        Intent startIntent=new Intent(MainActivity.this,WelcomeActivity.class);
        startActivity(startIntent);
        finish();
    }


















    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            sendToWelcome();
        }

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_setting) {

            Intent settingIntent=new Intent(MainActivity.this,SettingActivity.class);
            startActivity(settingIntent);

        } else if (id == R.id.nav_all_users) {

            Intent allUsersIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(allUsersIntent);
        }else if (id == R.id.nav_feed) {

                Intent feedIntent=new Intent(MainActivity.this,FeedActivity.class);
                startActivity(feedIntent);


        } else if (id == R.id.nav_logout) {

            FirebaseAuth.getInstance().signOut();
            sendToWelcome();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
