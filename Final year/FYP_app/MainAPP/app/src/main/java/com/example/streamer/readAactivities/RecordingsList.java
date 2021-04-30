package com.example.streamer.readAactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.example.streamer.R;
import com.example.streamer.Adapters.SongAdapter;
import com.example.streamer.SongInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingsList extends AppCompatActivity implements SongAdapter.OnItemClickListener {

    public static final String AUDIO_DIR ="/Streaming Audios/";

    private ArrayList<SongInfo> _songs = new ArrayList<SongInfo>();
    RecyclerView recyclerView;
    SeekBar seekBar;
    SongAdapter songAdapter;
    private Handler myHandler = new Handler();
    ProgressDialog mDialog;
    Timer timer;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordings_list);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading files ....");
        mDialog.show();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setActivated(false);
//        _songs.add(new SongInfo("test","testname","https://file-examples-com.github.io/uploads/2017/11/file_example_OOG_1MG.ogg"));
//        _songs.add(new SongInfo("test","testname","https://file-examples-com.github.io/uploads/2017/11/file_example_OOG_1MG.ogg"));
//        _songs.add(new SongInfo("test","testname","https://file-examples-com.github.io/uploads/2017/11/file_example_OOG_1MG.ogg"));
//        _songs.add(new SongInfo("test","testname","https://file-examples-com.github.io/uploads/2017/11/file_example_OOG_1MG.ogg"));
//        _songs.add(new SongInfo("test","testname","https://file-examples-com.github.io/uploads/2017/11/file_example_OOG_1MG.ogg"));
        songAdapter = new SongAdapter(RecordingsList.this, _songs);
        recyclerView.setAdapter(songAdapter);
        songAdapter.setOnItemClickListener(this);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        checkUserPermission();


    }


    private void startProgress(){

        timer=new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        },0,500);

    }

    private void stopProgress(){

        timer.cancel();
    }


    private void checkUserPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                return;
            }
        }
        loadSongs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    checkUserPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }


    void loadSongs() {
        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("audio");

        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference items : listResult.getItems()) {
                            // All the prefixes under listRef.
                            System.out.println("Upload is lisrt " + items.getName());


                            items.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mDialog.dismiss();

                                    System.out.println("Upload is lisrt uuu " + uri.toString());

                                    SongInfo s = new SongInfo(items.getName(), items.getName(), uri.toString());
                                    _songs.add(s);
                                    songAdapter.notifyDataSetChanged();

                                }
                            });


                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                    }
                });
    }




    public void downloadManager(String mediaLink ){


        String mDownloadFileName=Uri.parse(mediaLink).getLastPathSegment();
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(mediaLink));

        String filesPath=getExternalFilesDir(null).toString()+ AUDIO_DIR + mDownloadFileName;


        File destFile = new File(filesPath.replace("Android/data/" + getPackageName() + "/files/", ""));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            destFile = new File(filesPath);
        }


        request .setTitle(mDownloadFileName);
        request .setDescription("Downloading..");
        request .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


        request.setDestinationUri(Uri.fromFile(destFile));
        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        Toast.makeText(this,"Download Started",Toast.LENGTH_LONG).show();



    }


    @Override
    public void onDownloadClick(int position) {

        downloadManager(_songs.get(position).getSongUrl());
    }

    @Override
    public void onItemClick(int position,View view) {

        SongInfo obj = _songs.get(position);
        Button b = (Button) view;

        try {


            if (b.getText().equals("Stop")) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
                b.setText("Play");
                stopProgress();
            } else {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setDataSource(obj.getSongUrl());
                            mediaPlayer.prepareAsync();

                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    startProgress();
                                    mp.start();
                                    seekBar.setProgress(0);
                                    seekBar.setMax(mediaPlayer.getDuration());
                                    Log.d("Prog", "run: " + mediaPlayer.getDuration());
                                }

                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    b.setText("Play");

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            stopProgress();
                                        }
                                    }, 500);

                                    //Toast.makeText(RecordingsList.this, "Voice Finished..", Toast.LENGTH_SHORT).show();
                                }
                            });
                            b.setText("Stop");


                        } catch (Exception e) {
                        }
                    }

                };
                myHandler.postDelayed(runnable, 100);

            }
        } catch (Exception e) {
            b.setText("Play");
        }
    }


}

