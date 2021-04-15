package com.victorolasupo.assignment4;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Registration extends AppCompatActivity {
    EditText emaillogin, passwordlogin;
    Button LOGIN;
    ProgressDialog progressDialog;
    String email;
    String password;
    database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        emaillogin = findViewById(R.id.input_email);
        passwordlogin = findViewById(R.id.input_password);
        LOGIN = findViewById(R.id.btn_login);
        db = new database(com.diroidd.app.shopping.Registration.this);
        LOGIN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    public void login() {

        if (!validate()) {

            return;
        }

        LOGIN.setEnabled(false);

        progressDialog = new ProgressDialog(com.diroidd.app.shopping.Registration.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registration...");
        progressDialog.show();

        email = emaillogin.getText().toString();
        password = passwordlogin.getText().toString();
//        Toast.makeText(this, email+password+"", Toast.LENGTH_SHORT).show();
        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed

                        LOGIN.setEnabled(true);
                        db.insert_logindata(email, password);
                        Toast.makeText(com.diroidd.app.shopping.Registration.this, "Register Successfully", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        // onLoginFailed();

                    }
                }, 2000);
    }


    public boolean validate() {
        boolean valid = true;

        String email = emaillogin.getText().toString();
        String password = passwordlogin.getText().toString();

        if (email.isEmpty()||email.length()<6) {
            emaillogin.setError("Enter 6 Letter Name");
            valid = false;
            Animation animShake = AnimationUtils.loadAnimation(com.diroidd.app.shopping.Registration.this, R.anim.one);
            LOGIN.startAnimation(animShake);
        } else {
            emaillogin.setError(null);
        }

        if (password.isEmpty()||password.length()<6||password.contains(" ")) {
            passwordlogin.setError("Enter 6 Letter Password Without Space");
            valid = false;
            Animation animShake = AnimationUtils.loadAnimation(com.diroidd.app.shopping.Registration.this, R.anim.one);
            LOGIN.startAnimation(animShake);
        } else {
            passwordlogin.setError(null);
        }

        return valid;
    }

}
