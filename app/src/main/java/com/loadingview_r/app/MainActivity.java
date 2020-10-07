package com.loadingview_r.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.loadingview_r.app.view.LoadingViewR;

public class MainActivity extends AppCompatActivity {
    LoadingViewR loadingViewR;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingViewR = findViewById(R.id.LoadingView_r);
        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingViewR.startMoving();
            }
        });

    }
}