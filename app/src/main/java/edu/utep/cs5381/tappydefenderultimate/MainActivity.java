package edu.utep.cs5381.tappydefenderultimate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = getSharedPreferences("HiScores", MODE_PRIVATE);

        Button playButton = findViewById(R.id.playButton);
        final TextView textFastestTime = (TextView) findViewById(R.id.textHighScore);
        long fastestTime = preferences.getLong("fastestTime", 1000000);
        // Put highscore
        textFastestTime.setText("Fastest Time:" + fastestTime);
        playButton.setOnClickListener(view -> {
            startActivity(new Intent(this, GameActivity.class));
            finish();
        });
    }
}
