package com.example.streamer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.streamer.Adapters.MessagesAdapter;
import com.example.streamer.Models.ChatMessage;
import com.example.streamer.Models.Chats;
import com.example.streamer.Models.Message;
import com.example.streamer.Models.Notification;
import com.example.streamer.Models.User;
import com.example.streamer.Utility.Utility;
import com.example.streamer.audioRecorder.RecorderHelper;
import com.example.streamer.retrofit.ApiClient;
import com.example.streamer.retrofit.ApiInterface;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;

public class MessagesActivity extends AppCompatActivity
        implements RecorderHelper.AudioListener,MessagesAdapter.onItemClickListener {

    private final int AUDIO_REQUEST=200;
    private final int IMAGE_REQUEST=100;
    private final String MESSAGES_COLLECTION="Messages";
    private final String CHATS_COLLECTION="Chats";
    private final String FIRE_BASE_SERVER_KEY="AAAAylyP5Zk:APA91bGIsPGaS2OfOCCTNVduVOdRcXeMR2bWHeKUkqTK5SoHO6Wqbhfwh8NrobIEPyA7zGotYkxvLuBdz8XmkHAVtIY__Wus34tbNia0uMGkruB4IvOpnGRtvu2POsB5uMC3f1foL0vV";



    private Uri filePath=null;
    FirebaseStorage storage;
    StorageReference storageReference;

    DatabaseReference UsersReference;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestoreDB;
    CollectionReference chatsCollection;
    ApiInterface apiClient;
    SeekBar lastSeekBar;
    ImageView lastBtn;
    Timer timer;
    MediaPlayer mediaPlayer;



    RecyclerView recyclerView;
    TextView textViewWarning;
    ProgressBar progressBar, sendMessageProgress,audioProgress;
    List<ChatMessage> chatMessageList;
    MessagesAdapter messagesAdapter;
    String chatId=null;
    String receiverId,receiverName,receiverToken;
    boolean newChat=true;
    EditText messageBox;
    ImageView sendBtn;
    TextView textReceiverName,textViewEmptyList;
    SharedPrefs sharedPrefs;
    RecordButton recordButton;
    RecordView recordView;
    RelativeLayout messageLayout;
    ImageView audioBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        sharedPrefs=new SharedPrefs(this);
        initViews();
        initData();
        initRecyclerView();
        initFirebase();

    }


    private void initViews(){

        audioBtn=findViewById(R.id.audioBtn);
        textViewEmptyList=findViewById(R.id.textViewEmptyList);
        sendMessageProgress =findViewById(R.id.messageProgressBar);
        messageBox=findViewById(R.id.editTextMessage);
        sendBtn=findViewById(R.id.sendBtn);
        progressBar=findViewById(R.id.progressBar);
        audioProgress=findViewById(R.id.audioProgressBar);
        recordButton=findViewById(R.id.record_button);
        recordView=findViewById(R.id.record_view);
        messageLayout=findViewById(R.id.messageLayout);

        textReceiverName =findViewById(R.id.username);


        showMessage();
        showProgress();
        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(messageBox.getText().toString().isEmpty()){
                    toast ("Write something");
                }
                else
                {
                    startMessage(false,messageBox.getText().toString());
                }
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(messageBox.getText().toString().isEmpty()){
                    toast ("Write something");
                }
                else
                {
                    if(!newChat&&chatId==null){
                        toast ("Something wrong try again later");
                    }else startMessage(false,messageBox.getText().toString());
                }
            }
        });

        findViewById(R.id.pictureBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utility.checkPermission(MessagesActivity.this)) {
                    if (!newChat && chatId == null) {
                        toast("Something wrong try again later");
                    } else startMessage(true, null);
                }
            }
        });

        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Utility.checkPermission(MessagesActivity.this)){
               selectAudio();
                }
            }
        });

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        RecorderHelper.initializeAudioRecording(this,recordView,
                recordButton,messageLayout);

        RecorderHelper.setAudioListener(this);
    }

    private void initRecyclerView() {
        recyclerView=findViewById(R.id.chatRecyclerView);
        chatMessageList =new ArrayList<>();
        messagesAdapter =new MessagesAdapter(chatMessageList,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(messagesAdapter);
        messagesAdapter.setOnItemClickListener(this);


    }

    private void initData(){

        UsersReference= FirebaseDatabase.getInstance().getReference("Users");

        Bundle extras=getIntent().getExtras();
        receiverId=extras.getString("receiverId");
        receiverToken=extras.getString("receiverToken");

        if(extras.containsKey("receiverName")){
            receiverName=extras.getString("receiverName");
            textReceiverName.setText(receiverName);
        }
        else
        {
         mGetUserInfo(receiverId);
        }


    }

    private void initFirebase() {


        firestoreDB=FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        auth= FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        chatsCollection=firestoreDB.collection(CHATS_COLLECTION);



        chatsCollection.
                whereArrayContains("usersList",
                        user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                hideProgress();

                if (task.isSuccessful()) {


                    for (QueryDocumentSnapshot document : task.getResult()) {

                        Chats chat=document.toObject(Chats.class);


                        List<String> user=chat.getUsersList();

                        if(user.contains(receiverId)){

                            newChat=false;
                            chatId=chat.getChatId();
                            mGetMessages();
                        }

                    }
                }
                else
                {

                    newChat=false;
                    chatId=null;
                }

            }
        });



    }


    private void mGetMessages(){


        chatsCollection.document(chatId).collection(MESSAGES_COLLECTION).
                orderBy("time", Query.Direction.ASCENDING).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                hideProgress();
                chatMessageList.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {


                    ChatMessage chatMessage=document.toObject(ChatMessage.class);

                    chatMessageList.add(chatMessage);

                }

                if(!chatMessageList.isEmpty()){hideMessage();}
                messagesAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(chatMessageList.size()-1);
            }
        });


    }


    private void startMessage(boolean image, String message){

        showMessageProgress();

        if(image){

            galleryIntent();
        }
        else
        {
           submitMessage(message,"","");
        }


    }

    private void submitMessage(String message,String image,String audio){


        if(newChat){

            chatId=chatsCollection.document().getId();

            Chats chat=new Chats(chatId,message,image,audio,Arrays.asList(receiverId, user.getUid()));

            chatsCollection.document(chatId).set(chat).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    newChat=false;
                    sendMessage(audio,image,message,chatId);
                }
            });

        }
        else
        {
            Map<String, Object> messageUpdate= new HashMap<>();
            messageUpdate.put("lastMessage" , message);
            messageUpdate.put("lastImage" , image);
            messageUpdate.put("lastAudio" , audio);


            chatsCollection.document(chatId).update(messageUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sendMessage(audio,image,message,chatId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideMessageProgress();
                    toast("Something wrong try again");
                }
            });


        }
    }


    private void sendMessage(String audio,String image,String message,String chatId){

        ChatMessage chatMessage=new ChatMessage(
                message,
                image,
                audio,
                String.valueOf(System.currentTimeMillis()),
                user.getUid(),receiverId);

        chatsCollection.document(chatId).
                collection(MESSAGES_COLLECTION).
                document().set(chatMessage).
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                hideMessageProgress();
                messageBox.setText("");
                chatMessageList.add(chatMessage);
                messagesAdapter.notifyItemInserted(chatMessageList.size()-1);
                recyclerView.smoothScrollToPosition(chatMessageList.size()-1);
                toast("Message sent");
                hideMessage();
                sentToNotification(message,image,audio);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideMessageProgress();
                toast("Something wrong try again");
            }
        });



    }


    private void sentToNotification(String message,String image,String audio) {

        String media="";

        if(!image.isEmpty()){media="Photo";}
        else if(!audio.isEmpty()){media="Audio";}

        apiClient=ApiClient.getClient("https://fcm.googleapis.com/").create(ApiInterface.class);

        String to = receiverToken;


        Notification data = new Notification(sharedPrefs.userName(),
                message,media,
                "OPEN_MESSAGES_ACTIVITY");

        Message notification = new Message(to, data);


        Call<Message> call = apiClient.sendMessage("key="+FIRE_BASE_SERVER_KEY, notification);

        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, retrofit2.Response<Message> response) { }

            @Override
            public void onFailure(Call<Message> call, Throwable t) { }
        });
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction( Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select Image"),IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if(requestCode==IMAGE_REQUEST){
                onSelectFromGalleryResult(data,requestCode);
            }
            else if(requestCode==AUDIO_REQUEST){
                toast("Uploading audio..");
                uploadAudio(getAudioPath(data.getData()));
            }


        }
        else
        {
            hideMessageProgress();
        }
    }


    private void onSelectFromGalleryResult(Intent data, int requestCode) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                if(bm!=null){
                    filePath=data.getData();
                    uploadImage();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       else{
            toast ("Failed to upload image try again");
        }

    }


    private void uploadImage(){

        toast ("uploading image");

        StorageReference bucketReference=storageReference.child("image"+System.currentTimeMillis()+".png");
        bucketReference.putFile(filePath).
                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {


                                        submitMessage(messageBox.getText().toString(),task.getResult().toString(),"");

                                    }
                                });

                    }
                });
    }


    private void showMessageProgress(){
        sendMessageProgress.setVisibility(View.VISIBLE);
        sendBtn.setVisibility(View.GONE);
    }

    private void hideMessageProgress(){
        sendMessageProgress.setVisibility(View.GONE);
        sendBtn.setVisibility(View.VISIBLE);
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

    private void  toast(String  toastMessage){
        Toast.makeText(this,toastMessage,Toast.LENGTH_SHORT).show();
    }


    private void mGetUserInfo(String id){

        UsersReference.
                child(id).
                addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        User user=snapshot.getValue(User.class);
                        textReceiverName.setText(user.getUserName());
                        receiverToken=user.getTokenId();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onAudioRecorder(String audioFile) {

        recordButton.setVisibility(View.GONE);
        audioProgress.setVisibility(View.VISIBLE);
      uploadAudio(audioFile);
    }

    private void uploadAudio(String audioFile){


        UploadTask uploadTask;
        StorageMetadata metadata;

        Uri uri = Uri.fromFile(new File(audioFile));

        metadata = new StorageMetadata.Builder()
                .setContentType("audio/*")
                .build();

        uploadTask = storageReference.child("audio/"+uri.getLastPathSegment()).putFile(uri, metadata);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> audioTask) {

                        recordButton.setVisibility(View.VISIBLE);
                        audioProgress.setVisibility(View.GONE);

                        submitMessage(messageBox.getText().toString(),"",audioTask.getResult().toString());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        recordButton.setVisibility(View.VISIBLE);
                        audioProgress.setVisibility(View.GONE);

                        toast("Something wrong try again");
                    }
                });
            }
        });

    }

    @Override
    public void onItemClick(int position, View view) {

    }

    @Override
    public void onAudioClick(int position, SeekBar seekBar, ImageView playBtn) {

        if(playBtn.getContentDescription().equals("playing")){
            playBtn.setContentDescription("stop");

            stopPlayer(playBtn);

        }
        else
        {
            stopPlayer(lastBtn);
            playBtn.setContentDescription("playing");
            startPlayer(playBtn,seekBar,chatMessageList.get(position).getAudioUrl());


        }

        lastSeekBar=seekBar;
        lastBtn=playBtn;
    }

    private void startPlayer(ImageView btn, SeekBar seekBar, String audioUrl){

        Log.d("AudioPlayer", "started");

        Handler myHandler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer = new MediaPlayer();
                    Log.d("AudioPlayer", "url "+audioUrl);
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepareAsync();

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {

                            btn.setImageResource(R.drawable.ic_pause_white);
                            btn.setContentDescription("playing");

                            startProgress(seekBar);
                            mp.start();
                            seekBar.setProgress(0);
                            seekBar.setMax(mediaPlayer.getDuration());
                            Log.d("AudioPlayer", "prepared" );
                        }

                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            btn.setImageResource(R.drawable.ic_play_white);
                            btn.setContentDescription("stop");

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                }
                            }, 500);

                            //Toast.makeText(RecordingsList.this, "Voice Finished..", Toast.LENGTH_SHORT).show();
                        }
                    });



                } catch (Exception e) {

                    Log.d("AudioPlayer",e.getMessage());
                }

            }
        };

        myHandler.postDelayed(runnable, 100);
    }

    private void stopPlayer(ImageView btn) {

        if(mediaPlayer!=null) {

            stopProgress();
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            btn.setImageResource(R.drawable.ic_play_white);
            btn.setContentDescription("stop");
        }

    }

    private void startProgress(SeekBar seekBar) {

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }, 0, 500);

    }

    private void stopProgress() {

        if(timer!=null){ timer.cancel();}
    }


    private void selectAudio(){

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,AUDIO_REQUEST);
    }


    private String getAudioPath(Uri uri) {
        String[] data = {MediaStore.Audio.Media.DATA};
        CursorLoader loader = new CursorLoader(this, uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}