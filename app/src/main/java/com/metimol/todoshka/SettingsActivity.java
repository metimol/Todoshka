package com.metimol.todoshka;

import static com.metimol.todoshka.EditInfoActivity.USER_AVATAR_KEY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;

public class SettingsActivity extends AppCompatActivity implements ConfirmDeleteDialog.ConfirmDeleteListener {

    private SharedPreferences sharedPreferences;
    private TextView tvUserName;
    private ImageView ivAvatar;
    private MainViewModel viewModel;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        ImageView ivBack = findViewById(R.id.ivBack);
        tvUserName = findViewById(R.id.tvUserName);
        ConstraintLayout clEditInfo = findViewById(R.id.clEditInfo);
        ConstraintLayout clEditCategories = findViewById(R.id.clEditCategories);
        ConstraintLayout clRemoveCompletedTasks = findViewById(R.id.clRemoveCompletedTasks);
        LinearLayout rateUsButton = findViewById(R.id.rateUsButton);
        LinearLayout shareButton = findViewById(R.id.shareButton);
        ivAvatar = findViewById(R.id.ivAvatar);
        TextView tvVersion = findViewById(R.id.tvVersion);

        sharedPreferences = getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);

        loadAndSetUserInfo();

        ivBack.setOnClickListener(v -> finish());

        clEditInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditInfoActivity.class);
            startActivity(intent);
        });

        clEditCategories.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditCategoriesActivity.class);
            startActivity(intent);
        });

        clRemoveCompletedTasks.setOnClickListener(v -> {
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(ConfirmDeleteDialog.ACTION_DELETE_COMPLETED);
            dialog.show(getSupportFragmentManager(), ConfirmDeleteDialog.TAG);
        });

        tvVersion.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);

        rateUsButton.setOnClickListener(v -> {
            String url = "https://github.com/metimol/Todoshka";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));

            try {
                v.getContext().startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(v.getContext(), getString(R.string.cannot_open_link), Toast.LENGTH_SHORT).show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appUrl = "https://github.com/metimol/Todoshka";
                Intent sendIntent = getIntent(appUrl);
                Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_via));
                v.getContext().startActivity(shareIntent);
            }

            @NonNull
            private Intent getIntent(String appUrl) {
                String recommendationText = getString(R.string.share_message) + " " + appUrl;
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, recommendationText);
                sendIntent.setType("text/plain");
                return sendIntent;
            }
        });

        var settings_layout = findViewById(R.id.settings_activity_screen);
        ViewCompat.setOnApplyWindowInsetsListener(settings_layout, (view, insets) -> {
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAndSetUserInfo();
    }

    private void loadAndSetUserInfo() {
        String userName = sharedPreferences.getString(MainActivity.USER_NAME_KEY, "User");
        tvUserName.setText(userName);

        String avatarSelected = sharedPreferences.getString(USER_AVATAR_KEY, "men");
        switch (avatarSelected) {
            case "men":
                ivAvatar.setImageResource(R.drawable.ic_men_avatar);
                break;
            case "women":
                ivAvatar.setImageResource(R.drawable.ic_women_avatar);
                break;
        }
    }

    @Override
    public void onDeleteConfirmed(Category category) {}

    @Override
    public void onDeleteConfirmed(ToDo task) {}

    @Override
    public void onDeleteCompletedTasksConfirmed() {
        viewModel.deleteCompletedTodos();
        Toast.makeText(this, getString(R.string.completed_tasks_removed), Toast.LENGTH_SHORT).show();
    }
}
