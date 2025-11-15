package com.metimol.todoshka;

import static com.metimol.todoshka.MainActivity.USER_AVATAR_KEY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.metimol.todoshka.database.Category;
import com.metimol.todoshka.database.ToDo;

public class SettingsFragment extends Fragment implements ConfirmDeleteDialog.ConfirmDeleteListener {

    private SharedPreferences sharedPreferences;
    private TextView tvUserName;
    private ImageView ivAvatar;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        tvUserName = view.findViewById(R.id.tvUserName);
        ConstraintLayout clEditInfo = view.findViewById(R.id.clEditInfo);
        ConstraintLayout clEditCategories = view.findViewById(R.id.clEditCategories);
        ConstraintLayout clRemoveCompletedTasks = view.findViewById(R.id.clRemoveCompletedTasks);
        LinearLayout rateUsButton = view.findViewById(R.id.rateUsButton);
        LinearLayout shareButton = view.findViewById(R.id.shareButton);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        TextView tvVersion = view.findViewById(R.id.tvVersion);

        sharedPreferences = requireActivity().getSharedPreferences(GetStartedActivity.PREFS_NAME, Context.MODE_PRIVATE);

        loadAndSetUserInfo();

        ivBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        clEditInfo.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_editInfoFragment);
        });

        clEditCategories.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_editCategoriesFragment);
        });

        clRemoveCompletedTasks.setOnClickListener(v -> {
            ConfirmDeleteDialog dialog = ConfirmDeleteDialog.newInstance(ConfirmDeleteDialog.ACTION_DELETE_COMPLETED);
            dialog.show(getChildFragmentManager(), ConfirmDeleteDialog.TAG);
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

        var settings_layout = view.findViewById(R.id.settings_fragment_screen);
        ViewCompat.setOnApplyWindowInsetsListener(settings_layout, (v, insets) -> {
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

    @Override
    public void onResume() {
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
        Toast.makeText(requireContext(), getString(R.string.completed_tasks_removed), Toast.LENGTH_SHORT).show();
    }
}