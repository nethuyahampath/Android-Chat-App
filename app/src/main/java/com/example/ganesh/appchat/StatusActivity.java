package com.example.ganesh.appchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private CardView mChangestatusBtn;
    private TextInputLayout mStatus;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mStatusProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().setTitle("Account Status");


        String status_value=getIntent().getStringExtra("status_value");

        mStatus=(TextInputLayout)findViewById(R.id.status_ti_status);
        mChangestatusBtn=(CardView) findViewById(R.id.status_cv_change_status_btn);

        mStatus.getEditText().setText(status_value);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);


        mChangestatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusProgress=new ProgressDialog(StatusActivity.this);
                mStatusProgress.setTitle("Changing Status");
                mStatusProgress.setMessage("Please Wait,Status is Changing...");
                mStatusProgress.show();

                String status=mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProgress.dismiss();
                            /*Intent statusIntent=new Intent(StatusActivity.this,SettingActivity.class);
                            statusIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(statusIntent);*/

                            Toast.makeText(getApplicationContext(),"Changes were done successfully",Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(getApplicationContext(),"Saving Error...Status Is Not Changed...Please Try Again..",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
