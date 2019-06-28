package com.example.ganesh.appchat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SetupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private ImageView setupImage;
    private Uri mainImageURI  = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mToolbar = (Toolbar) findViewById(R.id.setup_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Setup Activity");

        setupImage = findViewById(R.id.setup_image);

        setupImage.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick( View view ){

                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){

                    if(ContextCompat.checkSelfPermission(  SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != getPackageManager().PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this, "Permission Denied " , Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1 );

                    }else{
                        Toast.makeText(SetupActivity.this, "You Already Have Permission" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == RESULT_OK ){

        }

    }
}
