package com.example.streamer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.streamer.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    EditText uName, uEmail, uPassword, uConfirmPassword;
    Button registerBtn;
    CheckBox checkBoxTherapist;
    boolean isTherapists=false;

    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    ProgressBar progressBar;
    SharedPrefs sharedPrefs;
    Dialog dialogEmailSent ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPrefs=new SharedPrefs(this);
        TextView txt_signup = findViewById(R.id.txt_signup);
        uName = findViewById(R.id.username);
        uEmail = findViewById(R.id.email);
        uPassword = findViewById(R.id.password);
        uConfirmPassword = findViewById(R.id.Confirmpassword);
        checkBoxTherapist=findViewById(R.id.checkboxTherapist);
        dialogEmailSent=new Dialog(this);
        mAuth = FirebaseAuth.getInstance();
        registerBtn = findViewById(R.id.register_button);
        registerBtn.setOnClickListener(this);

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });




    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.register_button:
                //startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                registerUser();
                break;
        }

    }



    private void registerUser() {
        String fullName = uName.getText().toString().trim();
        String email = uEmail.getText().toString().trim();
        String password = uPassword.getText().toString().trim();
        String cPassword    = uConfirmPassword.getText().toString().trim();
        String image="Default";

        if(fullName.isEmpty()){
            uName.setError("Full name is required!");
            uName.requestFocus();
            return;
        }
        if(email.isEmpty()){
            uEmail.setError("Email is required!");
            uEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            uEmail.setError("Please provide a valid email!");
            uEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            uPassword.setError("Password  is required!");
            uPassword.requestFocus();
            return;
        }
        if(cPassword.isEmpty()){
            uConfirmPassword.setError("Password  is required!");
            uConfirmPassword.requestFocus();
            return;
        }

        if(checkBoxTherapist.isChecked()){
            isTherapists =true;
        }

  mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
      @Override
      public void onComplete(@NonNull Task<AuthResult> task) {

           if(task.isSuccessful()){

               String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
               FirebaseUser authUser=FirebaseAuth.getInstance().getCurrentUser();

               User user = new User(userId,fullName, email, cPassword,image, isTherapists,"");
               FirebaseDatabase.getInstance().getReference("Users").child(userId).setValue(user)
                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   authUser.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("sent", "Email sent.");
                                                    showDiaLogToVerifyEmail();
                                                }
                                            }
                                        });
//                                   sharedPrefs.setUserId(userId);
//                                   sharedPrefs.setUserEmail(email);
//                                   sharedPrefs.setUserImage(image);
//                                   sharedPrefs.setUserName(fullName);
//                                   sharedPrefs.setUserPhone(cPassword);

                                  // Toast.makeText(RegisterActivity.this, "You have been registered! ", Toast.LENGTH_LONG).show();


                               }

                               else{
                                   Toast.makeText(RegisterActivity.this, "Failed to register! Please try again later!", Toast.LENGTH_LONG).show();
                               }
                           }
                       });

           }
           else{
               Toast.makeText(RegisterActivity.this, "Register failed!", Toast.LENGTH_LONG).show();
           }


      }
  });



    }

    private void showDiaLogToVerifyEmail( ) {
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


}
