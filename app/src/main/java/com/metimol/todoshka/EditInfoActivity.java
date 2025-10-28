package com.metimol.todoshka;
import com.metimol.todoshka.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

public class EditInfoActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    public static final String USER_AVATAR_KEY = "user_avatar";

    private ShapeableImageView ivAvatarWomen;
    private ShapeableImageView ivAvatarMen;

    private ColorStateList purpleColor;
    private ColorStateList transparentColor;
    private float strokeSelectedPx;
    private int paddingSelectedPx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_info_activity);

        sharedPreferences = getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);

        ImageView ivBack = findViewById(R.id.ivBack);
        EditText etName = findViewById(R.id.etName);
        ImageView ivDone = findViewById(R.id.ivDone);

        ivAvatarWomen = findViewById(R.id.ivAvatarWomen);
        ivAvatarMen = findViewById(R.id.ivAvatarMen);
        LinearLayout llAvatarWomen = findViewById(R.id.llAvatarWomen);
        LinearLayout llAvatarMen = findViewById(R.id.llAvatarMen);

        initSelectionResources();

        if (sharedPreferences.contains(MainActivity.USER_NAME_KEY)) {
            String currentName = sharedPreferences.getString(MainActivity.USER_NAME_KEY, "User");
            etName.setText(currentName);
        }

        ivBack.setOnClickListener(v -> finish());

        ivDone.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();

            if (isValidName(newName)) {
                saveName(newName);
                finish();
            } else {
                Toast.makeText(EditInfoActivity.this, getString(R.string.short_name), Toast.LENGTH_SHORT).show();
            }
        });

        llAvatarWomen.setOnClickListener(v -> {
            updateAvatarSelection("women");
        });

        llAvatarMen.setOnClickListener(v -> {
            updateAvatarSelection("men");
        });

        String avatarSelected = sharedPreferences.getString(USER_AVATAR_KEY, "men");
        updateAvatarSelection(avatarSelected);


        var edit_info_layout = findViewById(R.id.edit_info_activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(edit_info_layout, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return WindowInsetsCompat.CONSUMED;
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

    private void saveAvatarSelection(String avatarSelected) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_AVATAR_KEY, avatarSelected);
        editor.apply();
    }

    private void updateAvatarSelection(String avatarSelected) {
        applySelectionStyle(avatarSelected);
        saveAvatarSelection(avatarSelected);
    }

    private void applySelectionStyle(String avatarSelected) {
        setAvatarStyle(ivAvatarWomen, avatarSelected.equals("women"));
        setAvatarStyle(ivAvatarMen, avatarSelected.equals("men"));
    }

    private void setAvatarStyle(ShapeableImageView imageView, boolean isSelected) {
        if (isSelected) {
            imageView.setStrokeWidth(strokeSelectedPx);
            imageView.setStrokeColor(purpleColor);
            imageView.setContentPadding(paddingSelectedPx, paddingSelectedPx, paddingSelectedPx, paddingSelectedPx);
        } else {
            imageView.setStrokeWidth(0f);
            imageView.setStrokeColor(transparentColor);
            imageView.setContentPadding(0, 0, 0, 0);
        }
    }

    private void initSelectionResources() {
        Context context = getApplicationContext();

        purpleColor = ContextCompat.getColorStateList(context, R.color.purple);
        transparentColor = ContextCompat.getColorStateList(context, android.R.color.transparent);

        paddingSelectedPx = (int) Utils.dpToPx(context, 5);
        strokeSelectedPx = Utils.dpToPx(context, 1);
    }
}