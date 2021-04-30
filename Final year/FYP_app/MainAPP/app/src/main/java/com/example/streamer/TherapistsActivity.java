package com.example.streamer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.streamer.Adapters.UserAdapter;
import com.example.streamer.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TherapistsActivity extends AppCompatActivity implements UserAdapter.onItemClickListener {


    List<User> userList=new ArrayList<>();
    RecyclerView usersRecyclerView;
    UserAdapter userAdapter;
    SharedPrefs sharedPrefs;
    TextView textViewEmptyList;
    ProgressBar progressBar;
    Dialog profileDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapists);

        sharedPrefs=new SharedPrefs(this);

        initViews();
        initRecyclerView();
        mGetData();

    }

    private void mGetData(){

        DatabaseReference dateRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = dateRef.orderByChild("therapists").equalTo(true);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                userList.clear();


                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    User user=postSnapshot.getValue(User.class);
                    userList.add(user);
                }

                updateMessage();

                hideProgress();
                userAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void initViews(){

        progressBar=findViewById(R.id.progressBar);
        textViewEmptyList=findViewById(R.id.textViewEmptyList);
        showProgress();
        showMessage();
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initRecyclerView() {
        usersRecyclerView=findViewById(R.id.recyclerView);
        userAdapter =new UserAdapter(userList,this);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        usersRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(usersRecyclerView.getContext(),
                layoutManager.getOrientation());
        usersRecyclerView.addItemDecoration(dividerItemDecoration);
        usersRecyclerView.setAdapter(userAdapter);
        userAdapter.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(int position, View view) {

        User user=userList.get(position);
        showProfileDialog(user);

    }



    private void showProfileDialog(User user) {

        profileDialog =new Dialog(this,R.style.FullScreenDialogStyle);
        profileDialog.setContentView(R.layout.dialog_therapists_profile);
        profileDialog.setCancelable(false);


        TextView textName,textEmail,textPhone;
        ImageView profileImage,closeBtn;

        closeBtn=profileDialog.findViewById(R.id.buttonCancel);
        profileImage=profileDialog.findViewById(R.id.profile_pic);
        textName= profileDialog.findViewById(R.id.textUserName);
        textEmail= profileDialog.findViewById(R.id.textUserEmail);
        textPhone= profileDialog.findViewById(R.id.textUserPhone);

        textName.setText(user.getUserName());
        textEmail.setText(user.getEmail());
        textPhone.setText(user.getPhone());

        Picasso.get().load(user.getImageUrl()).
                placeholder(R.drawable.default_profile).
                into(profileImage);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { profileDialog.dismiss();}
        });

        profileDialog.show();





    }


    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        progressBar.setVisibility(View.GONE);

    }

    private void updateMessage(){

        if(userList.isEmpty()){showMessage();}
        else{hideMessage();}
    }

    private void showMessage(){
        textViewEmptyList.setVisibility(View.VISIBLE);
    }

    private void hideMessage(){
        textViewEmptyList.setVisibility(View.GONE);

    }
}