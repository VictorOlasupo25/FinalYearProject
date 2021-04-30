package com.example.streamer.readAactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.streamer.R;

import java.io.File;
import java.util.UUID;

public class UploadVoiceActivity extends AppCompatActivity {

    private static final String LOG_TAG = "tag";
    ImageButton img_record;
    MediaRecorder recorder;
    TextView txt_recording,txt_record,txt_counter;
    Chronometer txt_timer;

    ProgressDialog mDialog;
    public int seconds = 60;
    public int minutes = 10;
    long maxTimeInMilliseconds = 300000;

    //firebase
    StorageReference mStorage;

    private String fileName = null;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String[] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private boolean audioRecordingPermissionGranted = false;
    private UploadTask uploadTask;
    private StorageMetadata metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_voice);


        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        mStorage = FirebaseStorage.getInstance().getReference();
        mDialog = new ProgressDialog(this);
        txt_record = findViewById(R.id.txt_view_record);
        txt_timer = findViewById(R.id.txt_counter);




        txt_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UploadVoiceActivity.this, RecordingsList.class));

            }
        });


        img_record = findViewById(R.id.recorder_btn);
        txt_recording = findViewById(R.id.txt_recording);

        img_record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    txt_recording.setText("Recording start...");
                    // startTimer(maxTimeInMilliseconds, 1000);
                    startRecording();

                    txt_timer.start();

                }else if(motionEvent.getAction() ==MotionEvent.ACTION_UP){


                    stopRecording();

                    txt_timer.stop();


                    txt_recording.setText("Recording Stopped!");
                }

                return false;
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                audioRecordingPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!audioRecordingPermissionGranted) {
            finish();
        }
    } //comit

    public void startTimer(final long finish, long tick) {
        CountDownTimer t;
        t = new CountDownTimer(finish, tick) {

            public void onTick(long millisUntilFinished) {
                long remainedSecs = millisUntilFinished / 1000;
                txt_counter.setText("" + (remainedSecs / 60) + ":" + (remainedSecs % 60));// manage it accordign to you
            }

            public void onFinish() {
                txt_counter.setText("00:00:00");
                Toast.makeText(UploadVoiceActivity.this, "Finish", Toast.LENGTH_SHORT).show();

                cancel();
            }
        }.start();
    }

    private void startRecording() {
        String uuid = UUID.randomUUID().toString();
        if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Streamrs").exists()){
            new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Streamrs").mkdir();

        }
        fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Streamrs/" + uuid + ".mp3";
        Log.i(UploadVoiceActivity.class.getSimpleName(), fileName);
        Log.d("MyApp",fileName);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        recorder.setOutputFile(fileName);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            System.out.println("asd 5 print");
            recorder.start();
        } catch (Exception e) {
            System.out.println("dnkjhdsdkhfksdf "+e.getMessage());
        }



    }
    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            System.out.println("asd 7 print");
            uploadData();
        }catch (Exception e){
            System.out.println("asd 7 print ee "+e.getMessage());

        }
    }



    private void uploadData() {
        mDialog.setMessage("Saving file....");
        mDialog.show();
        Uri uri = Uri.fromFile(new File(fileName));
        Log.d("MyApp",String.valueOf(uri));
        StorageReference ref = FirebaseStorage.getInstance().getReference();
//        ref.child("Audios").child(System.currentTimeMillis()+".3gp").putFile(uri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                       // while (!uriTask.isSuccessful()) ;
//
//                        if (uriTask.isSuccessful()) {
//                            mDialog.dismiss();
//
//                            txt_recording.setText("Voice Saved");
//                            fileName = "";
//                        }else {
//                            mDialog.dismiss();
//
//                            txt_recording.setText("Voice Failed to upload");
//                          //  fileName = "";
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("AddPostActivity", "TEST onFailure()...\nException : " + e.getMessage());
//            }
//        });
        metadata = new StorageMetadata.Builder()
                .setContentType("audio/mpeg")
                .build();

        uploadTask = ref.child("audio/"+uri.getLastPathSegment()).putFile(uri, metadata);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
                Log.d("MyApp","Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
                mDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Upload is failed");
                mDialog.dismiss();
                Log.d("MyApp","Failed : "+exception.getMessage());

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is done");
                mDialog.dismiss();
//
//                            txt_recording.setText("Voice Saved");
//                            fileName = "";
                //   Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
            }
        });
    }

}