package com.example.streamer.readAactivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.streamer.HomeActivity;
import com.example.streamer.R;

public class readADashboard extends AppCompatActivity {
    CardView basicSound, readingPractice, backButton;
    TextView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_a_dashboard);

        init();
        listeners();

    }

    private void init() {
        basicSound = findViewById(R.id.basic_sound);
        readingPractice = findViewById(R.id.reading_practice);
        backButton = findViewById(R.id.back_button);
        backBtn=findViewById(R.id.backBtn);

    }

    private void listeners() {

        basicSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // startActivity(new Intent(readADashboard.this, ButtonScreen.class));
                Intent intent = new Intent(readADashboard.this, ButtonScreen.class);
                startActivity(intent);

            }
        });

        readingPractice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(readADashboard.this, UploadVoiceActivity.class);
                startActivity(intent);


            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });
    }
    public void openNewActivity(){
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


}