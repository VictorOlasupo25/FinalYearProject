package com.victorolasupo.assignment4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText emaillogin, passwordlogin;
    Button LOGIN;
    ProgressDialog progressDialog;
    String email;
    String password;
    TextView linksignup;
    database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emaillogin = findViewById(R.id.input_email);
        passwordlogin = findViewById(R.id.input_password);
        LOGIN = findViewById(R.id.btn_login);
        db = new database(MainActivity.this);
        linksignup = findViewById(R.id.link_signup);
        linksignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Registration.class));
            }
        });
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

        progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Checking Record...");
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
                        if (db.Authenticate(email, password) == true) {
                            Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, InsertData.class);
                            intent.putExtra("getname",email);
                            startActivity(intent);
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                        }

                        // onLoginFailed();

                    }
                }, 2000);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emaillogin.getText().toString();
        String password = passwordlogin.getText().toString();


        if (email.isEmpty()) {
            emaillogin.setError("Fill UserName");
            valid = false;
            Animation animShake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.one);
            LOGIN.startAnimation(animShake);
        } else {
            emaillogin.setError(null);
        }

        if (password.isEmpty()) {
            passwordlogin.setError("Enter Password");
            valid = false;
            Animation animShake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.one);
            LOGIN.startAnimation(animShake);
        } else {
            passwordlogin.setError(null);
        }

        return valid;
    }
}