package com.metimol.todoshka;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.metimol.todoshka.database.ToDo;

public class ConfirmDeleteDialog extends DialogFragment {

    public static final String TAG = "ConfirmDeleteDialog";
    private static final String ARG_TASK = "task_to_delete";

    private ToDo taskToDelete;

    public interface ConfirmDeleteListener {
        void onDeleteConfirmed(ToDo task);
    }

    private ConfirmDeleteListener listener;

    public static ConfirmDeleteDialog newInstance(ToDo task) {
        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskToDelete = getArguments().getParcelable(ARG_TASK);
        }

        try {
            listener = (ConfirmDeleteListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ConfirmDeleteListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return inflater.inflate(R.layout.dialog_confirm_delete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnDelete = view.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dismiss());

        btnDelete.setOnClickListener(v -> {
            if (listener != null && taskToDelete != null) {
                listener.onDeleteConfirmed(taskToDelete);
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}