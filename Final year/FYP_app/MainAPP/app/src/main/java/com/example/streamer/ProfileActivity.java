package com.example.streamer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.streamer.Adapters.PostAdapter;
import com.example.streamer.Models.Post;
import com.example.streamer.Utility.Utility;
import com.example.streamer.audioRecorder.RecorderHelper;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.example.streamer.Helper.PostHelper.isLiked;
import static com.example.streamer.Helper.PostHelper.setLiked;

public class ProfileActivity extends AppCompatActivity implements
        PostAdapter.onItemClickListener, RecorderHelper.AudioListener {



    private final int AUDIO_REQUEST=200;
    private final String POSTS_COLLECTION="Posts";

    RecyclerView recyclerView;
    ImageView imageViewPostDialogImage,profileImage;
    Utility utility=new Utility();
    private final int SELECT_FILE = 1, PROFILE_PIC=2;
    private Uri filePath=null;

    EditText editName,editPhone,editEmail;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference userProfileReference;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore firestoreDB;
    CollectionReference postsCollection;

    EditText editTextCaption;
    Button buttonSubmit;
    ImageView buttonCancel;
    ProgressBar postProgressBar;
    Dialog dialog;


    TextView textViewWarning;
    ProgressBar progressBar;
    List<Post> userPosts;
    PostAdapter postAdapter;
    TextView textViewUserName,textViewUserEmail,textViewPhoneNumber;
    SharedPrefs sharedPrefs;
    Dialog updateDialog;

    MediaRecorder recorder;
    String audioFile;
    boolean audioRecorded=false;
    private UploadTask uploadTask;
    private StorageMetadata metadata;
    Timer timer;
    MediaPlayer mediaPlayer;
    SeekBar lastSeekBar;
    ImageView lastBtn;
    boolean imageSelected=false;
    TextView audioFileBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPrefs=new SharedPrefs(this);

        initView();
        initFirebase();
        getUserProfile();
        initRecyclerView();
        getAllPost();
    }


    private void getUserProfile() {

        textViewUserName.setText(sharedPrefs.userName());
        textViewUserEmail.setText(sharedPrefs.userEmail());
        textViewPhoneNumber.setText(sharedPrefs.userPhone());

        if(!sharedPrefs.userImage().isEmpty()) {

            Picasso.get().load(sharedPrefs.userImage()).
                    placeholder(R.drawable.default_profile).
                    into(profileImage);
        }

    }

    private void initRecyclerView() {
        userPosts=new ArrayList<>();
        postAdapter =new PostAdapter(userPosts,this,true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(postAdapter);
        postAdapter.setOnItemClickListener(this);

    }


    private void deleteItem(int position){

        new AlertDialog.Builder(ProfileActivity.this)
                .setMessage("Are you sure to delete post : "+userPosts.get(position).getCaption())
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        postsCollection.document(userPosts.get(position).getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                toast("Successfully deleted");
                                dialog.dismiss();

                                userPosts.remove(userPosts.get(position));
                                postAdapter.notifyDataSetChanged();


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                toast("Please try again ");
                                dialog.dismiss();
                            }
                        });


                    }
                })
                .show();


    }

    private void getAllPost() {

        postsCollection.
                whereEqualTo("userId",user.getUid()).
                orderBy("time", Query.Direction.DESCENDING).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        hideProgress();

                        if(task.isSuccessful()) {

                            userPosts.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {


                                Post post = document.toObject(Post.class);

                                userPosts.add(post);

                            }

                            if (!userPosts.isEmpty()) { hideMessage(); }
                            else{showMessage();}

                            postAdapter.notifyDataSetChanged();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.v("ProfileAc",""+e.getMessage());
            }
        });




    }




    private void initFirebase() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        auth= FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        firestoreDB=FirebaseFirestore.getInstance();
        postsCollection =firestoreDB.collection(POSTS_COLLECTION);

        userProfileReference= FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(user.getUid());

    }

    private void initView() {

        textViewPhoneNumber=findViewById(R.id.textViewUserPhone);
        profileImage=findViewById(R.id.profile_pic);
        recyclerView=findViewById(R.id.recyclerView);
        textViewWarning=findViewById(R.id.textViewEmptyList);
        progressBar=findViewById(R.id.progressBar);
        textViewUserName=findViewById(R.id.textViewUserName);
        textViewUserEmail=findViewById(R.id.textViewUserEmail);
        findViewById(R.id.buttonAddNewPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecorded=false;
                imageSelected=false;
                loadAddNewPostDialog();
            }
        });

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish();}
        });

        findViewById(R.id.editBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showUpdateDialog();}
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                galleryIntent(PROFILE_PIC);
            }
        });
    }

    private void loadAddNewPostDialog() {
        dialog=new Dialog(this,R.style.FullScreenDialogStyle);
        dialog.setContentView(R.layout.dialog_add_new_post);
        dialog.setCancelable(false);
        dialog.show();
        editTextCaption=dialog.findViewById(R.id.editTextCaption);
        buttonCancel=dialog.findViewById(R.id.buttonCancel);
        buttonSubmit=dialog.findViewById(R.id.buttonSubmit);
        postProgressBar=dialog.findViewById(R.id.postProgressBar);
        imageViewPostDialogImage=dialog.findViewById(R.id.imageViewPost);
        audioFileBtn=dialog.findViewById(R.id.audio_file);


        audioFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAudio();
            }
        });

        RecorderHelper.initializeAudioRecording(this,dialog.findViewById(R.id.record_view),
                dialog.findViewById(R.id.record_button),null);

        RecorderHelper.setAudioListener(this);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!editTextCaption.getText().toString().equals("")||imageSelected||audioRecorded){
                    postProgressBar.setVisibility(View.VISIBLE);

                    if(imageSelected){uploadImage();}
                    else if(audioRecorded){uploadAudio("");}
                    else{uploadData("","");}


                }else {
                    toast("Write something to post.");
                }
            }
        });

        imageViewPostDialogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(Utility.checkPermission(ProfileActivity.this)){
                    galleryIntent(SELECT_FILE);
                }


            }
        });


    }

    private void uploadImage(){

        StorageReference bucketReference=storageReference.child("image"+System.currentTimeMillis()+".png");
        bucketReference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {



                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> imageTask) {

                                if(audioRecorded){
                                    uploadAudio(imageTask.getResult().toString());
                                }
                                else
                                {
                                    uploadData(imageTask.getResult().toString(),"");
                                }

                            }
                        });


            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                postProgressBar.setVisibility(View.GONE);
                toast("Application Failed to upload your image : "+e.getMessage());
            }
        });

    }
    private void uploadAudio(String imageUrl){

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

                        uploadData(imageUrl,
                                audioTask.getResult().toString());
                    }
                });
            }
        });

    }



    private void uploadData(String imageUrl,String audioUrl){

        String postId=postsCollection.document().getId();

        Post post=new Post(
                postId,
                editTextCaption.getText().toString(),
                imageUrl,
                audioUrl,
                user.getUid(),
                String.valueOf(System.currentTimeMillis()),
                new ArrayList<>());

        postsCollection.document(postId).set(post).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        postProgressBar.setVisibility(View.GONE);
                        filePath=null;
                        toast("Successfully added your post");
                        dialog.dismiss();
                        userPosts.add(0,post);
                        postAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                postProgressBar.setVisibility(View.GONE);
                toast("Some thing going wrong please try again");
            }
        });

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
            startPlayer(playBtn,seekBar,userPosts.get(position).getAudioUrl());

        }

        lastSeekBar=seekBar;
        lastBtn=playBtn;
    }

    private void startPlayer(ImageView btn, SeekBar seekBar, String audioUrl){

        Handler myHandler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepareAsync();

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            btn.setImageResource(R.drawable.ic_pause);
                            btn.setContentDescription("playing");
                            startProgress(seekBar);
                            mp.start();
                            seekBar.setProgress(0);
                            seekBar.setMax(mediaPlayer.getDuration());
                            Log.d("Prog", "run: " + mediaPlayer.getDuration());
                        }

                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            btn.setImageResource(R.drawable.ic_play);
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
            btn.setImageResource(R.drawable.ic_play);
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

        if(timer!=null){
        timer.cancel();}
    }




    private void showUpdateDialog() {

        updateDialog =new Dialog(this,R.style.FullScreenDialogStyle);
        updateDialog.setContentView(R.layout.dialog_update_profile);
        updateDialog.setCancelable(false);


        Button buttonUpdate;
        ImageView buttonCancel;
        final ProgressBar postProgressBar;
        editName= updateDialog.findViewById(R.id.editUserName);
        editPhone= updateDialog.findViewById(R.id.editUserPhone);
        editEmail= updateDialog.findViewById(R.id.editUserEmail);
        buttonCancel= updateDialog.findViewById(R.id.buttonCancel);
        buttonUpdate= updateDialog.findViewById(R.id.buttonSubmit);
        postProgressBar= updateDialog.findViewById(R.id.postProgressBar);

        editName.setText(sharedPrefs.userName());
        editEmail.setText(sharedPrefs.userEmail());
        editPhone.setText(sharedPrefs.userPhone());

        updateDialog.show();


        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editName.getText().toString().isEmpty()){
                    toast ("Enter user name");
                }
                else
                if(editEmail.getText().toString().isEmpty()){
                    toast ("Enter email address");
                }
                else
                if(editPhone.getText().toString().isEmpty()){
                    toast ("Enter phone number");
                }
                else
                {
                    updateProfile(null,null);
                }
            }
        });



    }


    private void updateProfile(Bitmap bm,String imageUrl){

        Map<String, Object> childUpdates;

        if(imageUrl!=null){

            childUpdates = new HashMap<>();
            childUpdates.put("imageUrl" , imageUrl);
        }
        else
        {

            childUpdates = new HashMap<>();
            childUpdates.put("userName" , editName.getText().toString());
            childUpdates.put("email", editEmail.getText().toString());
            childUpdates.put("phone", editPhone.getText().toString());

        }


        userProfileReference.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if(imageUrl!=null){
                    sharedPrefs.setUserImage(imageUrl);
                    profileImage.setImageBitmap(bm);
                }
                else
                {

                    sharedPrefs.setUserPhone(editPhone.getText().toString());
                    sharedPrefs.setUserEmail(editEmail.getText().toString());
                    sharedPrefs.setUserName(editName.getText().toString());


                    getUserProfile();
                }

                toast ("Profile updated successfully");

                if(updateDialog!=null){ updateDialog.dismiss();}

                postAdapter.notifyDataSetChanged();

            }
        });

    }

    private void galleryIntent(int intentType)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction( Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),intentType);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE||requestCode == PROFILE_PIC) {
                onSelectFromGalleryResult(data,requestCode);
            }
            else
            if (requestCode == AUDIO_REQUEST) {

                audioRecorded=true;
                audioFile=getAudioPath(data.getData());
                audioFileBtn.setText(audioFile);

            }

        }
    }
    private void onSelectFromGalleryResult(Intent data, int requestCode) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                if(bm!=null){
                    imageSelected=true;
                    filePath=data.getData();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(bm!=null){

            if(requestCode==SELECT_FILE){
                imageViewPostDialogImage.setImageBitmap(bm);
            }
            else
            {
                updateProfilePic(bm);
            }


        }else{
            toast ("Failed to upload image try again");
        }

    }
    private void  toast(String  toastMessage){
        Toast.makeText(this,toastMessage,Toast.LENGTH_SHORT).show();
    }


    private void updateProfilePic(Bitmap bm){

        toast ("Updating profile picture");

        StorageReference bucketReference=storageReference.child("image"+System.currentTimeMillis()+".png");
        bucketReference.putFile(filePath).
               addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                       Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl()
                               .addOnCompleteListener(new OnCompleteListener<Uri>() {
                           @Override
                           public void onComplete(@NonNull Task<Uri> task) {

                               updateProfile(bm,task.getResult().toString());

                           }
                       });

                   }
               });
    }



    @Override
    public void onItemClick(int position, View view) {

        if(view.getId()==R.id.like){

            Post post=userPosts.get(position);


            if(post.getLikesList().contains(user.getUid())){

                markLiked(position,post,false);
            }
            else
            {
                markLiked(position,post,true);
            }
        }
        else if(view.getId()==R.id.imageViewDelete){

            deleteItem(position);
        }
    }

    private void markLiked(int position,Post post,boolean isLiked){


        List<String> likesList=userPosts.get(position).getLikesList();

        if(isLiked){ likesList.add(user.getUid());}
        else{likesList.remove(user.getUid());}

        Map<String, Object> messageUpdate= new HashMap<>();
        messageUpdate.put("likesList" , likesList);

        postsCollection.document(post.getId()).update(messageUpdate).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        post.setLikesList(likesList);
                        userPosts.set(position, post);
                        postAdapter.notifyDataSetChanged();
                    }
                });

    }



    private void showProgress(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        progressBar.setVisibility(View.GONE);

    }

    private void showMessage(){
        textViewWarning.setVisibility(View.VISIBLE);
    }

    private void hideMessage(){
        textViewWarning.setVisibility(View.GONE);

    }

    @Override
    public void onAudioRecorder(String audioFile) {
        audioRecorded=true;
        this.audioFile=audioFile;
        audioFileBtn.setText(audioFile);
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