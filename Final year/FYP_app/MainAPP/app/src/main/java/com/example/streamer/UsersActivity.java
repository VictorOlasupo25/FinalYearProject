package com.example.streamer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.streamer.Adapters.UserAdapter;
import com.example.streamer.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserAdapter.onItemClickListener {


    List<User> userList=new ArrayList<>();
    EditText searchBox;
    RecyclerView usersRecyclerView;
    UserAdapter userAdapter;
    SharedPrefs sharedPrefs;
    TextView textViewEmptyList;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        sharedPrefs=new SharedPrefs(this);

        initViews();
        initRecyclerView();
        setUpSearch();

    }

    private void setUpSearch(){

        searchBox=findViewById(R.id.editTextSearch);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {



                if(s.length()>0) {

                    showProgress();

                    DatabaseReference dateRef = FirebaseDatabase.getInstance().getReference().child("Users");
                    Query query = dateRef.orderByChild("userName").startAt(s.toString().trim()).endAt(s.toString().trim()+"\uf8ff");

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
                else
                {
                    userList.clear();
                    userAdapter.notifyDataSetChanged();
                    hideProgress();
                    updateMessage();

                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void initViews(){

        progressBar=findViewById(R.id.progressBar);
        textViewEmptyList=findViewById(R.id.textViewEmptyList);
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

        if(!userList.get(position).getId().equals(sharedPrefs.userId())){
            startChat(position);
            finish();
        }
    }


    private void startChat(int position){

        Intent intent=new Intent(this, MessagesActivity.class);
        intent.putExtra("receiverId",userList.get(position).getId());
        intent.putExtra("receiverName",userList.get(position).getUserName());
        intent.putExtra("receiverToken",userList.get(position).getTokenId());
        startActivity(intent);
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