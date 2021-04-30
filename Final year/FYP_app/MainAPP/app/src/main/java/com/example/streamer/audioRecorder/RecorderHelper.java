package com.example.streamer.audioRecorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;

import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.example.streamer.R;
import com.example.streamer.Utility.Utility;

import java.io.File;
import java.util.UUID;

public class RecorderHelper {

    public static MediaRecorder recorder;
    public static String tempAudioFile;
    public static AudioListener audioListener;


    public static void initializeAudioRecording(Context context, RecordView recordView,
                                                RecordButton recordButton, RelativeLayout messageLayout) {


        //IMPORTANT
        recordButton.setRecordView(recordView);

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {

                updateMessageLayout(messageLayout,false);
                if (Utility.checkPermission(context)) {
                    startRecording();
                }
            }

            @Override
            public void onCancel() {
                updateMessageLayout(messageLayout,true);
                stopRecording();
                deleteFile(new File(tempAudioFile));
            }

            @Override
            public void onFinish(long recordTime) {
                updateMessageLayout(messageLayout,true);
                stopRecording();
                audioListener.onAudioRecorder(tempAudioFile);

            }

            @Override
            public void onLessThanSecond()
            {
                updateMessageLayout(messageLayout,true);
                stopRecording();
                deleteFile(new File(tempAudioFile));
            }
        });
    }


    private static void startRecording() {
        String uuid = UUID.randomUUID().toString();
        if (!new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Streamrs").exists()) {
            new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Streamrs").mkdir();

        }

        tempAudioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/Streamrs/" + uuid + ".mp3";


        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        recorder.setOutputFile(tempAudioFile);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();

            recorder.start();
        } catch (Exception e) {

        }


    }

    private static void stopRecording() {
        try {
            recorder.stop();
            recorder.release();

        } catch (Exception e) {


        }
    }

    private static void deleteFile(File file) {
        file.delete();
    }


    public interface AudioListener{
        void onAudioRecorder(String audioFile);
    }

    public static void setAudioListener(AudioListener mAudioListener){
        audioListener=mAudioListener;
    }

    private static void updateMessageLayout(RelativeLayout messageLayout,boolean show){

        if(messageLayout!=null){

            if(show){messageLayout.setVisibility(View.VISIBLE);}
            else{messageLayout.setVisibility(View.GONE);}
        }

    }

}
