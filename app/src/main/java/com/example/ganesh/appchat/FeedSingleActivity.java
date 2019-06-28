package com.example.ganesh.appchat;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class FeedSingleActivity extends AppCompatActivity {

    private String mpost_key = null;

    private static final int GALLERY_REQUEST =  1;

    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    private DatabaseReference mUpdateDatabase;

    private ImageView mSingleImage;
    private EditText mSingleTitle;
    private EditText mSingleDesc;
    private Button mSingleRemoveButton;
    private Button mSingleUpdateButton;

    //firebase authorization
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_single);

        /*mToolbar = (Toolbar) findViewById(R.id.single_post_toolbar);
        setSupportActionBar(mToolbar);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Post");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Feed");

        mAuth = FirebaseAuth.getInstance();

        mpost_key = getIntent().getExtras().getString("post_id");

        mUpdateDatabase = FirebaseDatabase.getInstance().getReference().child("Feed").child(mpost_key);

        mSingleTitle = (EditText) findViewById(R.id.single_title);
        mSingleDesc =  (EditText) findViewById(R.id.single_desc);
        mSingleImage = (ImageView) findViewById(R.id.single_image);
        mSingleRemoveButton  = (Button) findViewById(R.id.single_remove_button);
        mSingleUpdateButton = (Button) findViewById(R.id.single_update_btn);

        mDatabase.child(mpost_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("uid").getValue();

                mSingleTitle.setText(post_title);
                mSingleDesc.setText(post_desc);

                mSingleTitle.setEnabled(false);
                mSingleDesc.setEnabled(false);

                mSingleRemoveButton.setVisibility(View.INVISIBLE);
                mSingleUpdateButton.setVisibility(View.INVISIBLE);

                Picasso.get().load(post_image).tag(FeedSingleActivity.this).into(mSingleImage);

                if( mAuth.getCurrentUser().getUid().compareTo(post_uid ) == 0 ){

                    mSingleTitle.setEnabled(true);
                    mSingleDesc.setEnabled(true);

                    mSingleRemoveButton.setVisibility(View.VISIBLE);
                    mSingleUpdateButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSingleRemoveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                mDatabase.child(mpost_key).removeValue();

                Intent feed_intent = new Intent( FeedSingleActivity.this, FeedActivity.class );
                startActivity(feed_intent);
            }
        });

        mSingleUpdateButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick( View view ){

                String title = mSingleTitle.getText().toString();
                String desc = mSingleDesc.getText().toString();

                mUpdateDatabase.child("title").setValue(title);
                mUpdateDatabase.child("desc").setValue(desc);

                Intent feed_intent = new Intent( FeedSingleActivity.this, FeedActivity.class );
                startActivity(feed_intent);
            }

        });


    }
}
