package com.example.streamer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.streamer.Adapters.ChatsAdapter;
import com.example.streamer.Adapters.MessagesAdapter;
import com.example.streamer.Models.Chats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ChatsActivity extends AppCompatActivity
        implements ChatsAdapter.onItemClickListener {


    private final String CHATS_COLLECTION="Chats";


    RecyclerView recyclerView;
    List<Chats> chatsList;
    ChatsAdapter chatsAdapter;
    TextView textViewEmptyList;
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestoreDB;
    CollectionReference chatsCollection;

    SharedPrefs sharedPrefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        initViews();
        initRecyclerView();
        initFirebase();
    }



    private void initViews(){

        sharedPrefs=new SharedPrefs(this);
        progressBar=findViewById(R.id.progressBar);
        textViewEmptyList=findViewById(R.id.textViewEmptyList);
        showMessage();
        showProgress();

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.searchBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatsActivity.this,UsersActivity.class));
            }
        });
    }

    private void initRecyclerView() {

        recyclerView=findViewById(R.id.recyclerView);
        chatsList =new ArrayList<>();
        LinearLayoutManager layoutManager=new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        chatsAdapter =new ChatsAdapter(chatsList,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatsAdapter);
        chatsAdapter.setOnItemClickListener(this);
    }

    private void initFirebase() {


        firestoreDB=FirebaseFirestore.getInstance();

        auth= FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        chatsCollection=firestoreDB.collection(CHATS_COLLECTION);


        chatsCollection.
                whereArrayContains("usersList",
                        user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {


                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Chats chat=document.toObject(Chats.class);

                        chatsList.add(chat);

                    }


                    if(chatsList.isEmpty()){showMessage();}
                    else{hideMessage();}

                    hideProgress();
                    chatsAdapter.notifyDataSetChanged();
                }


            }
        });



    }


    @Override
    public void onItemClick(int position, View view) {

        mGetReceiverId(chatsList.get(position));

    }


    private void mGetReceiverId(Chats chat){


        if(sharedPrefs.userId().equals(chat.getUsersList().get(0))){
            startChat(chat.getUsersList().get(1));
        }
        else
        {
            startChat(chat.getUsersList().get(0));
        }
    }


    private void startChat(String receiverId){

        Intent intent=new Intent(this, MessagesActivity.class);
        intent.putExtra("receiverId",receiverId);
        startActivity(intent);
    }


    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        progressBar.setVisibility(View.GONE);

    }

    private void showMessage(){
        textViewEmptyList.setVisibility(View.VISIBLE);
    }

    private void hideMessage(){
        textViewEmptyList.setVisibility(View.GONE);

    }

}