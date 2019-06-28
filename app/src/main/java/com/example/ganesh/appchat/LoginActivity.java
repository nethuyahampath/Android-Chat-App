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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private CardView mLoginBtn;
    private TextView mError;

    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;

    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail=(EditText)findViewById(R.id.login_et_email);
        mPassword=(EditText)findViewById(R.id.login_et_password);
        mLoginBtn=(CardView)findViewById(R.id.login_cv_login_btn);
        mError=(TextView)findViewById(R.id.login_tv_error);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users");

        mLoginProgress = new ProgressDialog(this);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=mEmail.getText().toString();
                String password=mPassword.getText().toString();


                if( TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ){
                    mError.setText(R.string.login_error_empty);
                }else{

                    mLoginProgress.setTitle("Account Login");
                    mLoginProgress.setMessage("Please wait, Login To Your Account...");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();


                    loginUser(email,password);
                }



            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    mLoginProgress.dismiss();
                    String current_uid=mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_uid).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });


                }else{
                    mLoginProgress.hide();
                    mError.setText(R.string.login_error);
                    Toast.makeText(LoginActivity.this,"Please Check Your Network Connection",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
