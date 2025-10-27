package com.metimol.todoshka;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GetStartedActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "TodoshkaPrefs";
    public static final String IS_FIRST_LAUNCH_KEY = "IsFirstLaunch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!sharedPreferences.contains(MainActivity.USER_NAME_KEY)) {
            setContentView(R.layout.get_started_activity);

            Button btnGetStarted = findViewById(R.id.btnGetStarted);

            btnGetStarted.setOnClickListener(v -> {
                Intent intent = new Intent(GetStartedActivity.this, NameActivity.class);
                startActivity(intent);
            });

            var getStartedLayout = findViewById(R.id.get_started_activity_screen);
            ViewCompat.setOnApplyWindowInsetsListener(getStartedLayout, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );
                return WindowInsetsCompat.CONSUMED;
            });

        } else {
            launchMainActivity();
        }
    }

    private void launchMainActivity() {
        Intent intent = new Intent(GetStartedActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}