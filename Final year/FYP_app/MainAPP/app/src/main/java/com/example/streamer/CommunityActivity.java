package com.example.streamer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.streamer.Fragments.AllPostFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



import java.util.ArrayList;
import java.util.List;


public class CommunityActivity extends AppCompatActivity {

    FragmentTransaction ft;
    FragmentManager fm;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference userReference;
    String[] locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        LoadFragment(new AllPostFragment());

    }








    private void LoadFragment(Fragment fragment) {
        fm=getSupportFragmentManager();
        ft=fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
     }

    private boolean checkLocationPermissions() {
        int permissionResult;
        List<String> permissionsList = new ArrayList<>();
        for (String p : locationPermissions) {
            permissionResult = ContextCompat.checkSelfPermission(this, p);
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(p);
            }
        }
        if (!permissionsList.isEmpty()) { //this is okay yes
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]), 202);
            return false;
        }else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 102) {
            for (int i = 0; i < permissions.length; i++) {
                //   String permission = permissions[i];
                int outPutResult = grantResults[i];
                if (outPutResult== PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"permissions granted SuccessFully ",Toast.LENGTH_LONG).show();
                   //  LoadFragment(new LocationFragment());
                } else {
                    Toast.makeText(getApplicationContext(),"permissions denied ",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}