package com.metimol.todoshka;
import com.metimol.todoshka.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.imageview.ShapeableImageView;

public class EditInfoFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    public static final String USER_AVATAR_KEY = "user_avatar";

    private ShapeableImageView ivAvatarWomen;
    private ShapeableImageView ivAvatarMen;

    private ColorStateList purpleColor;
    private ColorStateList transparentColor;
    private float strokeSelectedPx;
    private int paddingSelectedPx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        EditText etName = view.findViewById(R.id.etName);
        ImageView ivDone = view.findViewById(R.id.ivDone);

        ivAvatarWomen = view.findViewById(R.id.ivAvatarWomen);
        ivAvatarMen = view.findViewById(R.id.ivAvatarMen);
        LinearLayout llAvatarWomen = view.findViewById(R.id.llAvatarWomen);
        LinearLayout llAvatarMen = view.findViewById(R.id.llAvatarMen);

        initSelectionResources();

        if (sharedPreferences.contains(MainActivity.USER_NAME_KEY)) {
            String currentName = sharedPreferences.getString(MainActivity.USER_NAME_KEY, "User");
            etName.setText(currentName);
        }

        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        ivDone.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();

            if (isValidName(newName)) {
                saveName(newName);
                Navigation.findNavController(v).popBackStack();
            } else {
                Toast.makeText(requireContext(), getString(R.string.short_name), Toast.LENGTH_SHORT).show();
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

        var edit_info_layout = view.findViewById(R.id.edit_info_fragment_screen);
        ViewCompat.setOnApplyWindowInsetsListener(edit_info_layout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
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
        Context context = requireContext();

        purpleColor = ContextCompat.getColorStateList(context, R.color.purple);
        transparentColor = ContextCompat.getColorStateList(context, android.R.color.transparent);

        paddingSelectedPx = (int) Utils.dpToPx(context, 5);
        strokeSelectedPx = Utils.dpToPx(context, 1);
    }
}