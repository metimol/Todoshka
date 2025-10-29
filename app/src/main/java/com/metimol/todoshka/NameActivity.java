package com.metimol.todoshka;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class NameActivity extends AppCompatActivity {

    private EditText etName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_activity);

        etName = findViewById(R.id.etName);
        MaterialButton btnSaveName = findViewById(R.id.btnGetStarted);

        sharedPreferences = getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(MainActivity.USER_NAME_KEY)) {
            String currentName = sharedPreferences.getString(MainActivity.USER_NAME_KEY, "");
            etName.setText(currentName);
            etName.setSelection(currentName.length());
        }

        btnSaveName.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (isValidName(name)) {
                saveNameAndMarkFirstLaunchComplete(name);
                launchMainActivity();
            } else {
                Toast.makeText(NameActivity.this, getString(R.string.short_name), Toast.LENGTH_SHORT).show();
            }
        });

        var nameLayout = findViewById(R.id.name_activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(nameLayout, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private boolean isValidName(String name) {
        return name != null && name.length() >= 5;
    }

    private void saveNameAndMarkFirstLaunchComplete(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainActivity.USER_NAME_KEY, name);
        editor.putBoolean(GetStartedActivity.IS_FIRST_LAUNCH_KEY, false);
        editor.apply();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(NameActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}