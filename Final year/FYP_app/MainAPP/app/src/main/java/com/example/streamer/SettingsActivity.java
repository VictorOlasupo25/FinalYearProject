package com.example.streamer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    SharedPrefs sharedPrefs;
    CircleImageView userImage;
    TextView userName;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPrefs=new SharedPrefs(this);

        findViewById(R.id.back_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.profileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this,ProfileActivity.class));
            }
        });

        findViewById(R.id.therapistsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this,TherapistsActivity.class));
            }
        });

        findViewById(R.id.logout_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               logout();
            }
        });

        findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail();
            }
        });

        initFirebase();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initUserInfo();
    }


    private void sendEmail() {

        String recepientEmail = "streamer@gmail.com";
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + recepientEmail));

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean isIntentSafe = activities.size() > 0;
        if(isIntentSafe){startActivityForResult(intent,200); }

    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setMessage("Are you to Sign out from Application?")
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(firebaseUser!=null){
                            firebaseAuth.signOut();
                            finishAffinity();
                            startActivity(new Intent(SettingsActivity.this,LoginActivity.class));
                        }
                    }
                })
                .show();
    }

    private void initFirebase() {
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();


    }

    private void initUserInfo(){

        userImage=findViewById(R.id.profile_pic);
        userName=findViewById(R.id.username);

        userName.setText(sharedPrefs.userName());

        if(!sharedPrefs.userImage().isEmpty()) {

            Picasso.get().load(sharedPrefs.userImage()).
                    placeholder(R.drawable.default_profile).
                    into(userImage);
        }
    }
}