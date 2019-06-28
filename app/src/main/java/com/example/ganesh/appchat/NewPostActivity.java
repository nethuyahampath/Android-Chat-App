package com.example.ganesh.appchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.Manifest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private static final int GALLERY_REQUEST =  1;
    private static final int PERMISSIONS_REQUEST_READ_STORAGE = 100;

    private ImageButton mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitBtn;

    private Button mToFeedBtn;

    private Uri mImageUri;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private StorageReference mStorage;

    //databse reference
    private DatabaseReference mDatabase;

    //progress dialog
    private ProgressDialog mProgressDialog;

    //Firebase Authorization
    private FirebaseAuth mAuth;

    //Firebase Current User
    private FirebaseUser mCurrentUser;

    //Firebase Database user
    private DatabaseReference mDatabseUser;

    //date and time
    private String currentDate;
    private String currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        //ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE} , PERMISSIONS_REQUEST_READ_STORAGE);

        /*mToolbar = (Toolbar) findViewById(R.id.new_post_page_toolbar);
        setSupportActionBar(mToolbar);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Post");

        //the progress bar
        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        //get the current user
        mCurrentUser = mAuth.getCurrentUser();

        //set up storage
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Feed");


        //storage to the specific user
        mDatabseUser = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid());

        //Test to feed
        mToFeedBtn  = (Button) findViewById(R.id.to_feed_btn);
        mToFeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent login_intent = new Intent(  NewPostActivity.this, FeedActivity.class);
                startActivity(login_intent);

            }
        });

        mPostTitle = (EditText) findViewById(R.id.titleField);
        mPostDesc = (EditText) findViewById(R.id.descField);

        mSubmitBtn = (Button) findViewById(R.id.submitBtn);

        mSelectImage = (ImageButton) findViewById(R.id.imageSelect);

        mSelectImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }

        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick( View view ){
                startPosting();
            }
        });

    }

    private void startPosting(){

        currentDate = getCurrentDate();
        currentTime = getCurrentTime();

        mProgressDialog.setMessage("Posting to Feed ... ");
        mProgressDialog.show();

        final String title_val = mPostTitle.getText().toString().trim();
        final String desc_val = mPostDesc.getText().toString().trim();

        if( !TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null ){

            //store the image in the storage
            StorageReference filepath  = mStorage.child("Feed_Images").child(mImageUri.getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final  Uri downloadUri = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPost = mDatabase.push();

                    mDatabseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newPost.child("title").setValue(title_val);
                            newPost.child("desc").setValue(desc_val);
                            newPost.child("image").setValue(downloadUri.toString());
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("date").setValue(currentDate);
                            newPost.child("time").setValue(currentTime);
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if( task.isSuccessful() ){
                                        startActivity( new Intent(NewPostActivity.this, FeedActivity.class));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mProgressDialog.dismiss();
                }
            });
        }else
        if( TextUtils.isEmpty(title_val) && TextUtils.isEmpty(desc_val) && mImageUri == null){
            Toast.makeText(NewPostActivity.this, "Cannot keep  the above fields empty", Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
        }else
        if( TextUtils.isEmpty(title_val) ){
            Toast.makeText(NewPostActivity.this, "Cannot keep  the title empty", Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
        }else
        if( TextUtils.isEmpty(desc_val) ){
            Toast.makeText(NewPostActivity.this, "Cannot keep  the description empty", Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
        }else
        if( mImageUri == null ){
            Toast.makeText(NewPostActivity.this, "Cannot keep  the image empty", Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == GALLERY_REQUEST && resultCode == RESULT_OK ){

            mImageUri = data.getData();

            mSelectImage.setImageURI(mImageUri);
        }

    }

    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String newDate = dateFormat.format(date);

        return newDate;
    }

    public static String getCurrentTime() {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return (sdf.format(cal.getTime()));
    }

    /*
        saveButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick( View view ){

                String title = editTextTitle.getText().toString();
                String description = editTextDescription.getText().toString();

                Map<String , Object > note = new HashMap<>();
                note.put(KEY_TITLE, title);
                note.put(KEY_DESCRIPTION , description);

                db.collection("Notebook").document("My First Note").set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(NewPostActivity.this, "Note Saved" , Toast.LENGTH_LONG ).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewPostActivity.this, "Note Saved" , Toast.LENGTH_LONG ).show();
                    }
                });
            }

        });
    */

}
