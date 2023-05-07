package com.safeseasonmyanmar.smarthome.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.safeseasonmyanmar.smarthome.MainActivity;
import com.safeseasonmyanmar.smarthome.R;

import org.jetbrains.annotations.NotNull;

public class DialogInput extends DialogFragment {

    private static final String ARG_PARAM = "title";

    //Set resource parameter
    private String mParam;

    public DialogInput(){

    }

    public static DialogInput newInstance(String title){
        DialogInput frag = new DialogInput();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getString(ARG_PARAM);
        }
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_input, null))
                .setTitle(mParam)
                .setPositiveButton("OK", (dialog, which) -> {
                    //ToDo input data code
                })
                .setNegativeButton("CANCEL", (dialog, which) -> getDialog().cancel());

        return builder.create();
    }
}
