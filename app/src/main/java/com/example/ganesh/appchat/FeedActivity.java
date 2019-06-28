package com.example.ganesh.appchat;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewDebug;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FeedActivity extends AppCompatActivity {

    private RecyclerView mFeedList;
    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    private FloatingActionButton mAddPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        //get the database reference
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Feed");

        /*mToolbar = (Toolbar) findViewById(R.id.feed_page_toolbar);
        setSupportActionBar(mToolbar);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Feed");

        mFeedList = (RecyclerView) findViewById(R.id.feed_list);
        mFeedList.setHasFixedSize(true);
        mFeedList.setLayoutManager(new LinearLayoutManager(this));

        mAddPostButton = (FloatingActionButton) findViewById(R.id.add_post);

        mAddPostButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick( View view ){
                Intent feed_intent = new Intent( FeedActivity.this, NewPostActivity.class );
                startActivity(feed_intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Feed, FeedViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Feed, FeedViewHolder>(

                Feed.class,
                R.layout.feed_row,
                FeedViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(FeedViewHolder viewHolder, Feed model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());

                viewHolder.mView.setOnClickListener( new View.OnClickListener(){

                    @Override
                    public void onClick( View view ){

                        Intent singleFeedIntent = new Intent(FeedActivity.this, FeedSingleActivity.class);
                        singleFeedIntent.putExtra("post_id", post_key);
                        startActivity(singleFeedIntent);
                    }
                });
            }
        };

        mFeedList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder{

            View mView;

            public FeedViewHolder(View itemView ){
                super(itemView);
                mView = itemView;

            }

            public void setTitle( String title ){
                TextView post_title = (TextView) mView.findViewById(R.id.single_title);
                post_title.setText(title);
            }

            public void setDesc(String desc){
                TextView post_desc = (TextView) mView.findViewById(R.id.single_desc);
                post_desc.setText(desc);
            }

            public void setImage(Context ctx, String image ){
                ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
                Picasso.get().load(image).tag(ctx).into(post_image);
            }

            public void setUsername( String username ){

                TextView post_username = (TextView) mView.findViewById(R.id.post_username);
                post_username.setText(username);
            }

            public void setDate( String date ){

                TextView post_username = (TextView) mView.findViewById(R.id.post_date);
                post_username.setText(date);
            }

            public void setTime( String time ){
                TextView post_username = (TextView) mView.findViewById(R.id.post_time);
                post_username.setText(time);
            }

    }

    /*
    private Toolbar mToolbar;
    private Button mSetup;

    private FirebaseAuth mAuth;

    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton addPostBtn;

    private StorageReference storageReference;

    private String current_user_id;

    public FeedActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.feed_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Feed");

        addPostBtn = findViewById(R.id.add_post_btn);

        addPostBtn.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View view){

                Intent newPostIntent = new Intent( FeedActivity.this, NewPostActivity.class );
                startActivity(newPostIntent);
            }

        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if( currentUser == null ){
            sendToStart();
        }else{

            current_user_id =  mAuth.getCurrentUser().getUid();

            Map<String, String> userMap = new HashMap<>();
            userMap.put("name", current_user_id );

            firebaseFirestore.collection("Users").document( current_user_id ).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if( task.isSuccessful() ){

                        if( !task.getResult().exists()){

                            Intent setupIntent = new Intent( FeedActivity.this, LoginActivity.class );
                            startActivity(setupIntent);
                            finish();
                        }

                    }else{

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(  FeedActivity.this, "Error : " + errorMessage , Toast.LENGTH_LONG   ).show();
                    }
                }
            });



        }

        mSetup = (Button) findViewById(R.id.start_setup_btn );

        mSetup.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View view){

                Intent setup_activity = new Intent( FeedActivity.this,  );
                startActivity(setup_activity);
            }
        });


    }

    private void sendToStart() {
        Intent startIntent = new Intent( FeedActivity.this, StartActivity.class );
        startActivity(startIntent);
        finish();
    }

    */
}
