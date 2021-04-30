package com.example.streamer.readAactivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.streamer.R;

public class M5 extends AppCompatActivity {
    Button next, previous;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m5);
        previous = findViewById(R.id.previous_button);
        next = findViewById(R.id.next_button);
        back = findViewById(R.id.back_button);

        Button play = findViewById(R.id.mic_button);
        MediaPlayer mp = MediaPlayer.create(this, R.raw.medal);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(M5.this, M6.class));
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(M5.this, M4.class));
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(M5.this, ButtonScreen.class));
            }
        });
    }
}