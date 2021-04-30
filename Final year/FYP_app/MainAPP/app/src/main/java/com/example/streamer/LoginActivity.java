package com.example.streamer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.streamer.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    EditText  uEmail, uPassword;
    Button loginBtn;
    private FirebaseAuth mAuth;
    SharedPrefs sharedPrefs;
    Dialog dialogNotVerified ;
    Dialog dialogEmailSent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        uEmail=findViewById(R.id.email);
        uPassword=findViewById(R.id.password);
        TextView txt_signup = findViewById(R.id.txt_signup);
        loginBtn = findViewById(R.id.btn_login);
        mAuth = FirebaseAuth.getInstance();
        sharedPrefs=new SharedPrefs(this);
        dialogNotVerified=new Dialog(this);
        dialogEmailSent=new Dialog(this);
        if(mAuth.getCurrentUser()!=null){
            if (mAuth.getCurrentUser().isEmailVerified()){
            startHome();
            return;
            }
            else {
                FirebaseAuth.getInstance().signOut();
            }
        }

        loginBtn.setOnClickListener(this);
        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_login:

                userLogin();
                break;
        }
    }

    private void userLogin() {
        String email = uEmail.getText().toString().trim();
        String password = uPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            uEmail.setError("email is Required.");

            return;
        }

        if (TextUtils.isEmpty(password)) {
            uPassword.setError("Password is Required.");
            return;
        }
        if(password.length() < 6 ){

            uPassword.setError(" minimum 6 Characters ");
            return;
        }


        // Authenticate User
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    checkIfEmailVerified();
                   // Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(LoginActivity.this, "Login failed! Please try again! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void mGetUserData(){


        FirebaseDatabase.getInstance().getReference("Users").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user=snapshot.getValue(User.class);

                sharedPrefs.setUserId(user.getId());
                sharedPrefs.setUserEmail(user.getEmail());
                sharedPrefs.setUserImage(user.getImageUrl());
                sharedPrefs.setUserName(user.getUserName());
                sharedPrefs.setUserPhone(user.getPhone());

                startHome();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(LoginActivity.this, "Login failed! Please try again! " + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void startHome(){
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            mGetUserData();
            Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.

            notifyToVerifyEmail();

            //restart this activity

        }
    }
    private void notifyToVerifyEmail( ) {

        dialogNotVerified.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNotVerified.setContentView(R.layout.custom_dialog_email_not_verified);
        Button okButton=dialogNotVerified.getWindow().findViewById(R.id.ok__Ndialog_button);
        Button reSendButton=dialogNotVerified.getWindow().findViewById(R.id.resend__dialog_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                dialogNotVerified.dismiss();
            }
        });
        reSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("sent", "Email sent.");
                                    showDiaLogToVerifyEmail();
                                }
                            }
                        });
            }
        });

        dialogNotVerified.show();
    }
    private void showDiaLogToVerifyEmail() {
        dialogEmailSent.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEmailSent.setContentView(R.layout.custom_email_verification_dialog);
        Button okButton=dialogEmailSent.getWindow().findViewById(R.id.ok__dialog_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                dialogEmailSent.dismiss();
            }
        });
        dialogEmailSent.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

//        if (!user.isEmailVerified())
//        {
//            FirebaseAuth.getInstance().signOut();
//        }

    }
}