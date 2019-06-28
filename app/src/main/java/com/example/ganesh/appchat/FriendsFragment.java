package com.example.ganesh.appchat;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_uid;

    private View mMainView;



    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView=inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList=(RecyclerView)mMainView.findViewById(R.id.friends_friend_list);
        mAuth=FirebaseAuth.getInstance();

        mCurrent_uid=mAuth.getCurrentUser().getUid();

        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrent_uid);
        mFriendDatabase.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDatabase.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder ViewHolder, Friends model, int position) {
                ViewHolder.setDate(model.getDate());

                final String list_user_id=getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName=dataSnapshot.child("name").getValue().toString();
                        String userImage=dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String userOnline=dataSnapshot.child("online").getValue().toString();
                            ViewHolder.setUserOnline(userOnline);
                        }




                        ViewHolder.setName(userName);
                        ViewHolder.setUsersImage(userImage,getContext());

                        ViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[]=new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which==0){
                                            Intent profile_intent=new Intent(getContext(),UsersProfileActivity.class);
                                            profile_intent.putExtra("user_id",list_user_id);
                                            startActivity(profile_intent);
                                        }
                                        if(which==1){

                                            Intent chat_intent=new Intent(getContext(),ChatActivity.class);
                                            chat_intent.putExtra("user_id",list_user_id);
                                            chat_intent.putExtra("user_name",userName);
                                            startActivity(chat_intent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mFriendList.setAdapter(friendsRecyclerViewAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FriendsViewHolder(View itemView){
            super(itemView);
            mView=itemView;
        }

        public void setDate(String date){
            TextView userStatusView=(TextView)mView.findViewById(R.id.user_single_status);
            userStatusView.setText(date);
        }

        public void setName(String name){
            TextView userNameView=(TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
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


        public void setUserOnline(String online_status){

            ImageView userOnlineView=(ImageView)mView.findViewById(R.id.user_single_online);

            if (online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }



    }
}
