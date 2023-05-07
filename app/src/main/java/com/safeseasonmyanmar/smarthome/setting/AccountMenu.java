package com.safeseasonmyanmar.smarthome.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.safeseasonmyanmar.smarthome.R;
import com.safeseasonmyanmar.smarthome.ui.DialogInput;

public class AccountMenu extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static String USER_NAME, EMAIL, PASSWORD, ACCOUNT_LEVEL;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Set Resource
    SettingTextView textView;
    TextView accName, accEmail,accPassword, accStatus;

    public AccountMenu() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            textView = (SettingTextView) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "need implement");
        }
    }

    // TODO: Rename and change types and number of parameters
    public static AccountMenu newInstance(String param1, String param2) {
        AccountMenu fragment = new AccountMenu();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_menu, container, false);

        //Set resource name
        accName = view.findViewById(R.id.accName);
        accEmail = view.findViewById(R.id.acceEmail);
        accPassword = view.findViewById(R.id.accPass);
        accStatus = view.findViewById(R.id.accStatus);

        accName.setOnClickListener(clickView ->{
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            new AlertDialog.Builder(getActivity())
                    .setTitle("Input Display Name")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) ->
                            USER_NAME= input.getText().toString())
                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel())
                    .show();
        });

        accEmail.setOnClickListener(emailView ->{
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            new AlertDialog.Builder(getActivity())
                    .setTitle("Input Email Address")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) ->
                            EMAIL = input.getText().toString())
                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel())
                    .show();
        });

        accPassword.setOnClickListener(passView ->{
            final EditText input = new EditText(getActivity());
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            new AlertDialog.Builder(getActivity())
                    .setTitle("Input Password")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) ->
                            PASSWORD = input.getText().toString())
                    .setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel())
                    .show();
        });

        accStatus.setOnClickListener(statusView ->{
            new AlertDialog.Builder(getActivity())
                    .setTitle("Select Account Leve")
                    .setItems(R.array.accountLevel, (dialog, which) -> {
                        ACCOUNT_LEVEL = getActivity().getResources().getStringArray(R.array.accountLevel)[which];
                    }).show();
        });

        return view;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onDestroy() {
        float pixels = getContext().getResources().getDisplayMetrics().density;
        final TextView textView = new TextView(getActivity());
        textView.setTextColor(R.color.secondary_text_color);

        StringBuilder stringBuilder = new StringBuilder();
        if (USER_NAME != null)
            stringBuilder.append("User name: ").append(USER_NAME).append('\n');
        if (EMAIL != null)
            stringBuilder.append("Email: ").append(EMAIL).append('\n');
        if (PASSWORD != null)
            stringBuilder.append("Password: ").append(PASSWORD).append('\n');
        if (ACCOUNT_LEVEL != null)
            stringBuilder.append("Account Level: ").append(ACCOUNT_LEVEL);
        textView.setText(stringBuilder);
        int start = (int) (30 * pixels + 0.5f);
        int end = (int) (30 * pixels + 0.5f);
        textView.setPadding(start,0,end,0);

        if (stringBuilder.length() != 0){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Input Confirmation")
                    .setMessage("Are you sure want to synchronize in server on below setting?")
                    .setView(textView)
                    .setPositiveButton("CONFIRM", (dialog, which) -> {
                        //ToDo synchronization in server
                    })
                    .setNegativeButton("NO", (dialog, which) -> {
                        dialog.cancel();
                    }).show();
        }
        super.onDestroy();
    }
}