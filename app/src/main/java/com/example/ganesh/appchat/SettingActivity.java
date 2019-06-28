package com.example.ganesh.appchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private FirebaseAuth mAuth;
	
	
    private DatabaseReference mUserRef;

    private CircleImageView mUserImage;
    private TextView mFullName;
    private TextView mUserStatus;

    private CardView mStatusBtn;
    private CardView mImageBtn;
    private CardView mPasswordBtn;

    private static final int IMAGE_PICK=1;

    private StorageReference mImageStorageReference;

    private ProgressDialog mImageProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setTitle("Settings");

        mUserImage=(CircleImageView)findViewById(R.id.setting_iv_user_image);
        mFullName=(TextView)findViewById(R.id.setting_tv_full_name);
        mUserStatus=(TextView)findViewById(R.id.setting_tv_user_status);
        mStatusBtn=(CardView)findViewById(R.id.setting_cv_change_status_btn);
        mImageBtn=(CardView )findViewById(R.id.setting_cv_change_image_btn);
        mPasswordBtn=(CardView) findViewById(R.id.setting_cv_change_password_btn);



        Toast.makeText(SettingActivity.this,"Working On Page Updating...",Toast.LENGTH_LONG).show();

        mImageStorageReference = FirebaseStorage.getInstance().getReference();

		
		
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();




        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());


        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(SettingActivity.this,dataSnapshot.toString(),Toast.LENGTH_LONG).show();

                String fullName=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();


                mFullName.setText(fullName);
                mUserStatus.setText(status);


                if(!image.equals("default")){

                    //Picasso.get().load(image).placeholder(R.drawable.user3).into(mImage);
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.user).into(mUserImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {

                            Picasso.get().load(image).placeholder(R.drawable.user).into(mUserImage);

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value=mUserStatus.getText().toString();
                Intent statusIntent=new Intent(SettingActivity.this,StatusActivity.class);
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent=new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"SELECT PROFILE IMAGE"),IMAGE_PICK);

               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);*/


            }
        });





    }

/*
   @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);


            mUserRef.child("online").setValue(true);


    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            mUserRef.child("online").setValue(false);
        }
    }

*/



    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        try {



            if(requestCode==IMAGE_PICK && resultCode==RESULT_OK){
                //String imageUri=data.getDataString();

                Uri imageUri=data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1,1)
                        .start(this);


                //Toast.makeText(SettingActivity.this,imageUri,Toast.LENGTH_LONG).show();
            }


            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    mImageProgress=new ProgressDialog(SettingActivity.this);
                    mImageProgress.setTitle("Uploading Profile Image...");
                    mImageProgress.setMessage("Please Wait, Profile Image is Uploading...");
                    mImageProgress.setCanceledOnTouchOutside(false);
                    mImageProgress.show();

                    Uri resultUri = result.getUri();

                    File thumb_filepath=new File(resultUri.getPath());

                    String current_user_id=mCurrentUser.getUid();


                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filepath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    StorageReference filePath=mImageStorageReference.child("profile_images").child(current_user_id+".jpg");

                    final StorageReference thumbFilePath=mImageStorageReference.child("profile_images").child("thumbs").child(current_user_id+".jpg");


                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                Toast.makeText(SettingActivity.this,"Still Working On It... Please Wait a While",Toast.LENGTH_LONG).show();

                                final String download_url=task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumbFilePath.putBytes(thumb_byte);

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {


                                        String thumb_download_url=thumb_task.getResult().getDownloadUrl().toString();

                                        if(thumb_task.isSuccessful()){

                                            Map update_hashMap=new HashMap<>();
                                            update_hashMap.put("image",download_url);
                                            update_hashMap.put("thumb_image",thumb_download_url);


                                            mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        mImageProgress.dismiss();
                                                        Toast.makeText(SettingActivity.this,"Profiel Image Uploaded Successfully",Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                            });
                                        }else {

                                            Toast.makeText(SettingActivity.this,"Error in uploading thumbnail",Toast.LENGTH_LONG).show();
                                            mImageProgress.dismiss();
                                        }




                                    }
                                });




                            }else{
                                Toast.makeText(SettingActivity.this,"Error In Uploading Profile Image",Toast.LENGTH_LONG).show();
                                mImageProgress.dismiss();
                            }
                        }
                    });

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }




}
