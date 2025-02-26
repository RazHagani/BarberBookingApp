package com.example.barberbookingapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.widget.Button;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.barberbookingapp.MainActivity;
import com.example.barberbookingapp.R;

/*
פרגמנט התחברות למערכת
זה הפרמגנט הפותח של האפליקציה
בפרגמנט זה נדרש שם משתמש וסיסמא
לאחר בדיקה שלהם אם המשתמש ספר הוא יובל לפרגמנט הספרים
אם המשתמש הוא לקוח הוא יובל לפרגמנט הבית
בנוסף קיים כפתור המוביל לפרגמנט של הרשמה בתור משתמש חדש
 */

public class LoginFragment extends Fragment {

    private EditText emailField, passwordField;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        //בעת לחיצה על כפתור הרשמה המשתמש מנווט אל פרגמנט בו ניתן לבצע הרשמה
        Button registerButton = view.findViewById(R.id.register_buttonLogin);
        registerButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });


        emailField = view.findViewById(R.id.emailLogin);
        passwordField = view.findViewById(R.id.passwordLogin);


        Button loginButton = view.findViewById(R.id.login_buttonLogin);
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            //התחברות המשתמש נעשת על ידי פונקציה הנמצאת במיין ולשם נשלחים האימייל והסיסמא של המשתמש
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.loginUser( email,  password);
        });


        return view;
    }
}