package com.example.ganesh.appchat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrent_uid;

    private TextView mNameView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;

    private Toolbar mChatToolbar;
    private ImageView mChatAddBtn;
    private ImageView mChatSendBtn;
    private EditText mChatMsg;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    private final List<Message> messageList=new ArrayList<>();
    private LinearLayoutManager mLinearlayout;
    private MessageAdapter mMessageAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD=10;
    private int mCurrentPage=1;

    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";

    private static final int IMAGE_PICK=1;
    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar=(Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar=getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);




        mRootRef= FirebaseDatabase.getInstance().getReference();

        mAuth=FirebaseAuth.getInstance();

        mCurrent_uid = mAuth.getCurrentUser().getUid();

        mChatUser=getIntent().getStringExtra("user_id");
        String userName=getIntent().getStringExtra("user_name");

        getSupportActionBar().setTitle(userName);

        LayoutInflater inflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_app_bar_chat,null);

        actionBar.setCustomView(action_bar_view);


        mNameView=(TextView)findViewById(R.id.chat_bar_name);
        mLastSeenView=(TextView)findViewById(R.id.chat_bar_last_seen);
        mProfileImage=(CircleImageView)findViewById(R.id.chat_bar_image);
        mChatAddBtn=(ImageView)findViewById(R.id.chat_bottom_iv_add);
        mChatSendBtn=(ImageView)findViewById(R.id.chat_bottom_iv_send);
        mChatMsg=(EditText)findViewById(R.id.chat_bottom_tv_msg);

        mMessageAdapter=new MessageAdapter(messageList);

        mMessagesList=(RecyclerView)findViewById(R.id.chat_messages_list);
        mRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);
        mLinearlayout=new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearlayout);
        mMessagesList.setAdapter(mMessageAdapter);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mRootRef.child("chat").child(mCurrent_uid).child(mChatUser).child("seen").setValue(true);

        loadMessages();

        mNameView.setText(userName);


        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online=dataSnapshot.child("online").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    mLastSeenView.setText("online");
                }else{
                    GetTimeAgo getTimeAgo=new GetTimeAgo();

                    long lastTime=Long.parseLong(online);
                    String lastSeenTime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        mRootRef.child("chat").child(mCurrent_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap=new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap=new HashMap();
                    chatUserMap.put("chat/" + mCurrent_uid + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("chat/" + mChatUser + "/" + mCurrent_uid,chatAddMap);


                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError!=null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }

                        }
                    });

                }




            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(imageIntent,"SELECT IMAGE"),IMAGE_PICK);
            }
        });



        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });









    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==IMAGE_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();

            final String current_user_ref="messages/" + mCurrent_uid +"/" +mChatUser;
            final String chat_user_ref="messages/" + mChatUser +"/" +mCurrent_uid;


            DatabaseReference user_message_push=mRootRef.child("messages")
                    .child(mCurrent_uid).child(mChatUser).push();

            final String push_id=user_message_push.getKey();


            StorageReference filepath=mImageStorage.child("message_images").child(push_id+".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        String download_url=task.getResult().getDownloadUrl().toString();

                        Map messageMap=new HashMap();
                        messageMap.put("message",download_url);
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("from",mCurrent_uid);

                        Map messageUserMap=new HashMap();
                        messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                        messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                        mChatMsg.setText("");


                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if (databaseError!=null){
                                    Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                }
                            }
                        });

                    }



                }


            });
        }
    }







    private void loadMoreMessages() {
        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrent_uid).child(mChatUser);
        Query messageQuery=messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Message message=dataSnapshot.getValue(Message.class);
                String messageKey=dataSnapshot.getKey();


                if(!mPrevKey.equals(messageKey)){

                    messageList.add(itemPos++,message);

                }else {
                    mPrevKey=mLastKey;
                }



                if(itemPos==1){

                    mLastKey=messageKey;


                }





                Log.d("TOTALKEYS","Last Key : " + mLastKey + "| Prev Key : " + mPrevKey + " | Message Key : " + messageKey);



                mMessageAdapter.notifyDataSetChanged();


                mRefreshLayout.setRefreshing(false);

                mLinearlayout.scrollToPositionWithOffset(10,0);



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void loadMessages() {

        DatabaseReference messageRef=mRootRef.child("messages").child(mCurrent_uid).child(mChatUser);
        Query messageQuery=messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message=dataSnapshot.getValue(Message.class);

                itemPos++;

                if(itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    mLastKey=messageKey;
                    mPrevKey=messageKey;
                }

                messageList.add(message);
                mMessageAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messageList.size()-1);

                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }






    private void sendMsg(){

        String message=mChatMsg.getText().toString();
        if(!TextUtils.isEmpty(message)){

            String current_user_ref="messages/"+mCurrent_uid+"/"+mChatUser;
            String chat_user_ref="messages/"+mChatUser+"/"+mCurrent_uid;

            DatabaseReference user_message_push=mRootRef.child("messages").child(mCurrent_uid).child(mChatUser).push();

            String push_id=user_message_push.getKey();

            Map messageMap=new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrent_uid);

            Map messageUserMap=new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id,messageMap);


            mChatMsg.setText("");


            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }







}
