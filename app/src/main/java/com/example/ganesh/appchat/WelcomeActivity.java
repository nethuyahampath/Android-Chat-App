package com.example.ganesh.appchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    private CardView mSignupBtn;
    private CardView mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mSignupBtn=(CardView)findViewById(R.id.welcome_cv_signup_btn);
        mLoginBtn=(CardView)findViewById(R.id.welcome_cv_login_btn);


        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent=new Intent(WelcomeActivity.this,SignupActivity.class);
                startActivity(signupIntent);
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent=new Intent(WelcomeActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }



}
