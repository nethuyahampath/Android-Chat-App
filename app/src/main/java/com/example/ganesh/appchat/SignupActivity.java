package com.example.ganesh.appchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private EditText mFullName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mConfirmPassword;
    private CardView mSignupBtn;
    private TextView mPasswordNotMatch;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mSignupProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFullName=(EditText)findViewById(R.id.signup_et_name);
        mEmail=(EditText)findViewById(R.id.signup_et_email);
        mPassword=(EditText)findViewById(R.id.signup_et_password);
        mConfirmPassword=(EditText)findViewById(R.id.signup_et_confirm_password);
        mSignupBtn=(CardView)findViewById(R.id.signup_cv_signup_btn);
        mPasswordNotMatch=(TextView)findViewById(R.id.signup_tv_password_not_match);

        mAuth = FirebaseAuth.getInstance();

        mSignupProgress = new ProgressDialog(this);



        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName=mFullName.getText().toString();
                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();
                String confirmPassword=mConfirmPassword.getText().toString();

                if( TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) ){
                    mPasswordNotMatch.setText(R.string.signup_empty_fields);
                }else if(!password.equals(confirmPassword)){
                    mPasswordNotMatch.setText(R.string.sigunp_password_not_match);
                }else{

                    mSignupProgress.setTitle("Registering User");
                    mSignupProgress.setMessage("Please wait, Your Account is Creating...");
                    mSignupProgress.setCanceledOnTouchOutside(false);
                    mSignupProgress.show();


                    signupUser(fullName,email,password);
                }
            }
        });


    }

    private void signupUser(final String fullName, final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String uid=current_user.getUid();

                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    mDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                    HashMap<String,String> userMap=new HashMap<>();
                    userMap.put("name",fullName);
                    userMap.put("email",email);
                    userMap.put("password",password);
                    userMap.put("status","Hi there ! I am using FactChat");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("device_token" , deviceToken);


                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mSignupProgress.dismiss();

                                Intent mainIntent=new Intent(SignupActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }else{
                    mSignupProgress.hide();
                    mPasswordNotMatch.setText(R.string.sigup_error);
                    Toast.makeText(SignupActivity.this,"Please Check Your Network Connection",Toast.LENGTH_LONG).show();
                }
            }
        });
    }




}
