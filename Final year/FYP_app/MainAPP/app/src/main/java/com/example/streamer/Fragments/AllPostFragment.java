package com.example.streamer.Fragments;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.streamer.ChatsActivity;
import com.example.streamer.MessagesActivity;
import com.example.streamer.SongInfo;
import com.example.streamer.audioRecorder.RecorderHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.streamer.Adapters.PostAdapter;
import com.example.streamer.Models.Post;
import com.example.streamer.R;
import com.example.streamer.Utility.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class AllPostFragment extends Fragment
        implements PostAdapter.onItemClickListener, RecorderHelper.AudioListener {

    private final String POSTS_COLLECTION="Posts";
    private final int AUDIO_REQUEST=200;


    EditText editTextCaption;
    Button buttonSubmit;
    ImageView buttonCancel;
    ProgressBar postProgressBar;
    Dialog dialog;

    View fragmentView;
    CardView buttonAddNewPost;
    FloatingActionButton chatBtn;
    RecyclerView recyclerView;
    ImageView imageViewPostDialogImage;
    Utility utility=new Utility();
    private final int  SELECT_FILE = 1;
    private Uri filePath=null;

    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseFirestore firestoreDB;
    CollectionReference postsCollection;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView textViewWarning;
    ProgressBar progressBar;
    List<Post> postsList;
    PostAdapter postAdapter;
    String audioFile;

    boolean audioRecorded=false;
    boolean imageSelected=false;
    private UploadTask uploadTask;
    private StorageMetadata metadata;
    Timer timer;
    MediaPlayer mediaPlayer;
    SeekBar lastSeekBar;
    ImageView lastBtn;
    TextView audioFileBtn;


    public AllPostFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       fragmentView= inflater.inflate(R.layout.fragment_all_post, container, false);
       initView();
       initFirebase();
       initRecyclerView();
       getAllPost();
       return fragmentView;

    }

    private void initRecyclerView() {
        postsList =new ArrayList<>();
        postAdapter =new PostAdapter(postsList,getContext(),false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(postAdapter);
        postAdapter.setOnItemClickListener(this);
    }

    private void getAllPost() {

        showProgress();

        postsCollection.
                orderBy("time", Query.Direction.DESCENDING).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {


                        hideProgress();
                        postsList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {


                            Post post=document.toObject(Post.class);

                            postsList.add(post);

                        }

                        if(!postsList.isEmpty()){hideMessage();}
                        else{showMessage();}
                        postAdapter.notifyDataSetChanged();
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

    }

    private void initView() {
        buttonAddNewPost=fragmentView.findViewById(R.id.buttonAddNewPost);
        chatBtn=fragmentView.findViewById(R.id.buttonChat);
        recyclerView=fragmentView.findViewById(R.id.recyclerView);
        textViewWarning=fragmentView.findViewById(R.id.textViewEmptyList);
        progressBar=fragmentView.findViewById(R.id.progressBar);
        buttonAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioRecorded=false;
                imageSelected=false;
                loadAddNewPostDialog();
            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ChatsActivity.class));
            }
        });
    }

    private void loadAddNewPostDialog() {
        dialog=new Dialog(getContext(),R.style.FullScreenDialogStyle);
        dialog.setContentView(R.layout.dialog_add_new_post);
        dialog.setCancelable(false);
        dialog.show();
        audioFileBtn=dialog.findViewById(R.id.audio_file);
        editTextCaption=dialog.findViewById(R.id.editTextCaption);
        buttonCancel=dialog.findViewById(R.id.buttonCancel);
        buttonSubmit=dialog.findViewById(R.id.buttonSubmit);
        postProgressBar=dialog.findViewById(R.id.postProgressBar);
        imageViewPostDialogImage=dialog.findViewById(R.id.imageViewPost);


        RecorderHelper.initializeAudioRecording(getContext(),dialog.findViewById(R.id.record_view),
                dialog.findViewById(R.id.record_button),null);

        RecorderHelper.setAudioListener(this);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             dialog.dismiss();
            }
        });

        audioFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(Utility.checkPermission(getContext())){
                selectAudio();}
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


               if(Utility.checkPermission(getContext())){
                   galleryIntent();
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

        Log.v("audioUri ",uri.toString());

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
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        postProgressBar.setVisibility(View.GONE);
                        toast("Some thing going wrong please try again");
                    }
                });
            }
        });

    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction( Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
            else
            if (requestCode == AUDIO_REQUEST) {

                audioRecorded=true;
                audioFile=getAudioPath(data.getData());
                audioFileBtn.setText(audioFile);

            }

        }
    }
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                if(bm!=null){
                    imageSelected=true;
                    filePath=data.getData();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(bm!=null){
            imageViewPostDialogImage.setImageBitmap(bm);
        }else{
          toast ("Failed to upload image try again");
        }

    }
    private void  toast(String  toastMessage){
        Toast.makeText(getContext(),toastMessage,Toast.LENGTH_SHORT).show();
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
                        postsList.add(0,post);
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
    public void onItemClick(int position, View view) {

        Post post= postsList.get(position);

        if(view.getId()==R.id.like){


            markLiked(position,post, !post.getLikesList().contains(user.getUid()));
        }

    }

    @Override
    public void onAudioClick(int position, SeekBar seekBar,ImageView playBtn) {


        if(playBtn.getContentDescription().equals("playing")){
            playBtn.setContentDescription("stop");

            playBtn.setImageResource(R.drawable.ic_play_circle);
            stopPlayer(playBtn);

        }
        else
        {
            playBtn.setImageResource(R.drawable.ic_pause_circle);
            stopPlayer(lastBtn);
            playBtn.setContentDescription("playing");
            startPlayer(playBtn,seekBar,postsList.get(position).getAudioUrl());


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

                            btn.setContentDescription("stop");

                            btn.setImageResource(R.drawable.ic_play_circle);

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

            private void startChat(String receiverId) {

                Intent intent = new Intent(getContext(), MessagesActivity.class);
                intent.putExtra("receiverId", receiverId);
                startActivity(intent);
            }

            private void markLiked(int position, Post post, boolean isLiked) {


                List<String> likesList = postsList.get(position).getLikesList();

                if (isLiked) {
                    likesList.add(user.getUid());
                } else {
                    likesList.remove(user.getUid());
                }

                Map<String, Object> messageUpdate = new HashMap<>();
                messageUpdate.put("likesList", likesList);

                postsCollection.document(post.getId()).update(messageUpdate).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                post.setLikesList(likesList);
                                postsList.set(position, post);
                                postAdapter.notifyDataSetChanged();
                            }
                        });

            }


            private void showProgress() {
                progressBar.setVisibility(View.VISIBLE);
            }

            private void hideProgress() {
                progressBar.setVisibility(View.GONE);

            }

            private void showMessage() {
                textViewWarning.setVisibility(View.VISIBLE);
            }

            private void hideMessage() {
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
        CursorLoader loader = new CursorLoader(getContext(), uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}