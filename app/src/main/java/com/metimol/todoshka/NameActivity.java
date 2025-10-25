package com.metimol.todoshka;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class NameActivity extends AppCompatActivity {

    private EditText etName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_activity);

        etName = findViewById(R.id.etName);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);

        sharedPreferences = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.contains(MainActivity.USER_NAME_KEY)) {
            String currentName = sharedPreferences.getString(MainActivity.USER_NAME_KEY, "");
            etName.setText(currentName);
            etName.setSelection(currentName.length());
        }

        btnGetStarted.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (isValidName(name)) {
                saveName(name);

                if (isTaskRoot()) {
                    Intent intent = new Intent(NameActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();

            } else {
                Toast.makeText(NameActivity.this, "The name must contain at least 5 letters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidName(String name) {
        return name != null && name.length() >= 5;
    }

    private void saveName(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainActivity.USER_NAME_KEY, name);
        editor.apply();
    }
}