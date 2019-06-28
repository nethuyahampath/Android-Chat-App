package com.example.ganesh.appchat;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private RecyclerView mAllUsersList;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mUsersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        getSupportActionBar().setTitle("All Users");

        mUsersDatabaseReference= FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabaseReference.keepSynced(true);

        mAllUsersList=(RecyclerView)findViewById(R.id.all_users_rv_users_list);
        mAllUsersList.setHasFixedSize(true);
        mAllUsersList.setLayoutManager(new LinearLayoutManager(this));


        Toast.makeText(AllUsersActivity.this,"Please Wait... Loading Users List...",Toast.LENGTH_LONG).show();

    }


    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabaseReference

        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setUsersStatus(model.getStatus());
                viewHolder.setUsersImage(model.getImage(),getApplicationContext());

                final String user_id=getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProgressDialog=new ProgressDialog(AllUsersActivity.this);
                        mProgressDialog.setTitle("Loading User Profile");
                        mProgressDialog.setMessage("Please Wait... Working On Profile Loading...");
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.show();



                        Intent profile_intent=new Intent(AllUsersActivity.this,UsersProfileActivity.class);
                        profile_intent.putExtra("user_id",user_id);
                        startActivity(profile_intent);
                        mProgressDialog.dismiss();
                    }
                });
            }
        };

        mAllUsersList.setAdapter(firebaseRecyclerAdapter);
    }






    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;


        public UsersViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setName(String name){
            TextView userNameView=(TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUsersStatus(String status){
            TextView userStatusView=(TextView)mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUsersImage(final String thumb_image, final Context context){
            final CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.user_single_image);

            //Picasso.with().load(url).placeholder(R.drawable.default_pic).into(imageView);
            //Picasso.get().load(thumb_image).placeholder(R.drawable.user).tag(context).into(userImageView);
            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.user).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(thumb_image).placeholder(R.drawable.user).tag(context).into(userImageView);

                }
            });

        }

    }



}
