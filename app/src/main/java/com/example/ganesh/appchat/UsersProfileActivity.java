package com.example.ganesh.appchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersProfileActivity extends AppCompatActivity {

    private CircleImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileDeclineRequestBtn;
    private Button mProfileAddFriendBtn;


    private DatabaseReference mUsersDatabase;
    private DatabaseReference mfriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;


    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;
    private String mCurrent_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);
        getSupportActionBar().setTitle("User Profile");


        final String user_id=getIntent().getStringExtra("user_id");

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mfriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("friend_requests");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef=FirebaseDatabase.getInstance().getReference();

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage=(CircleImageView) findViewById(R.id.profile_iv_user_image);
        mProfileName=(TextView)findViewById(R.id.profile_tv_full_name);
        mProfileStatus=(TextView)findViewById(R.id.profile_tv_user_status);
        //mProfileFriendsCount=(TextView)findViewById(R.id.profile_friend_count);
        mProfileAddFriendBtn=(Button) findViewById(R.id.profile_btn_add_friend);
        mProfileDeclineRequestBtn=(Button)findViewById(R.id.profile_btn_decline_request);

        mCurrent_state="not_friends";

        mProfileDeclineRequestBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineRequestBtn.setEnabled(false);

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Profile");
        mProgressDialog.setMessage("Please Wait... User Profile Is Loading...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.user).into(mProfileImage);

                //-------------FRIEND LIST / REQUEST FEATURE
                mfriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("recieved")){

                                mCurrent_state="request_recieved";
                                mProfileAddFriendBtn.setText("ACCEPT FREIND REQUEST");

                                mProfileDeclineRequestBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineRequestBtn.setEnabled(true);

                            }else if(req_type.equals("sent")){

                                mCurrent_state="request_sent";
                                mProfileAddFriendBtn.setText("CANCEL FREIND REQUEST");

                                mProfileDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();

                        }else{

                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state="friends";
                                        mProfileAddFriendBtn.setText("UNFRIEND THIS PERSON");

                                        mProfileDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRequestBtn.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                    String error=databaseError.getMessage();
                                    Toast.makeText(UsersProfileActivity.this, error ,Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String error=databaseError.getMessage();
                Toast.makeText(UsersProfileActivity.this,error ,Toast.LENGTH_LONG).show();
            }
        });


        mProfileAddFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileAddFriendBtn.setEnabled(false);


                // --  --------------NOT FRIEND STATE ---------------

                if(mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotificationReference=mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId=newNotificationReference.getKey();

                    HashMap<String,String> notificationData=new HashMap<>();
                    notificationData.put("from",mCurrentUser.getUid());
                    notificationData.put("type","request");

                    Map requestMap=new HashMap();
                    requestMap.put("friend_requests/" + mCurrentUser.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("friend_requests/" + user_id + "/" + mCurrentUser.getUid() + "/request_type","recieved");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId,notificationData);


                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError!=null){
                                String error=databaseError.getMessage();
                                Toast.makeText(UsersProfileActivity.this,"There was some error in sending request " + error ,Toast.LENGTH_LONG).show();
                            }

                            mProfileAddFriendBtn.setEnabled(true);
                            mCurrent_state="request_sent";
                            mProfileAddFriendBtn.setText("CANCEL FRIEND REQUEST");



                        }
                    });

                }


                // --  --------------CANCEL REQUEST STATE ---------------

                if(mCurrent_state.equals("request_sent")){

                    mfriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mfriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileAddFriendBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileAddFriendBtn.setText("SEND FRIEND REQUEST");

                                    mProfileDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineRequestBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }


                // -------- REQ RECIEVED STATE---------


                if(mCurrent_state.equals("request_recieved")){

                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap=new HashMap();
                    friendsMap.put("friends/" + mCurrentUser.getUid() + "/" + user_id + "/date" + "/", currentDate);
                    friendsMap.put("friends/" + user_id + "/" + mCurrentUser.getUid()  + "/date" + "/" , currentDate);

                    friendsMap.put("friend_requests/" + mCurrentUser.getUid() + "/" + user_id , null);
                    friendsMap.put("friend_requests/" + user_id + "/" + mCurrentUser.getUid() , null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null){
                                mProfileAddFriendBtn.setEnabled(true);
                                mCurrent_state="friends";
                                mProfileAddFriendBtn.setText("UNFREIND THIS PERSON");

                                mProfileDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestBtn.setEnabled(false);
                            }else{
                                String error=databaseError.getMessage();
                                Toast.makeText(UsersProfileActivity.this,error,Toast.LENGTH_LONG).show();

                            }


                        }


                    });




                }//if




                //-------------unfriend

                if(mCurrent_state.equals("friends")){

                    Map unFriendMap=new HashMap();
                    unFriendMap.put("friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unFriendMap.put("friends/" + user_id + "/" + mCurrentUser.getUid(), null);


                    mRootRef.updateChildren(unFriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null){

                                mCurrent_state="not_friends";
                                mProfileAddFriendBtn.setText("SEND FRIEND REQUEST");

                                mProfileDeclineRequestBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestBtn.setEnabled(false);
                            }else{
                                String error=databaseError.getMessage();
                                Toast.makeText(UsersProfileActivity.this,error,Toast.LENGTH_LONG).show();

                            }
                            mProfileAddFriendBtn.setEnabled(true);


                        }


                    });
                }



            }
        });

    }
}
