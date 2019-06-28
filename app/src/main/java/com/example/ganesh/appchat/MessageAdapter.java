package com.example.ganesh.appchat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ganesh on 9/24/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {






    private List<Message> mMessageList;
    private DatabaseReference mUserDatabase;


    public MessageAdapter(List<Message> mMessageList){
        this.mMessageList=mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent,int viewType){


        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);


    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public TextView messageName;
        //public TextView messageTime;
        public CircleImageView profileImage;
        public ImageView messageSendImage;

        public MessageViewHolder(View view){
            super(view);

            messageText=(TextView)view.findViewById(R.id.message_message_text);
            messageName=(TextView)view.findViewById(R.id.message_name_text);
            //messageTime=(TextView)view.findViewById(R.id.message_time_text);
            profileImage=(CircleImageView)view.findViewById(R.id.message_image);
            messageSendImage=(ImageView)view.findViewById(R.id.message_send_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i){

        Message c=mMessageList.get(i);

        String from_user=c.getFrom();
        String message_type=c.getType();


        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name=dataSnapshot.child("name").getValue().toString();
                String image=dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.messageName.setText(name);

                Picasso.get().load(image).tag(viewHolder.profileImage.getContext()).placeholder(R.drawable.user).into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")){
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageSendImage.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage()).tag(viewHolder.profileImage.getContext()).placeholder(R.drawable.user).into(viewHolder.messageSendImage);


        }



    }



    @Override
    public int getItemCount(){
        return mMessageList.size();
    }

}
