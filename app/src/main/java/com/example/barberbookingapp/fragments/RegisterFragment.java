package com.example.barberbookingapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.barberbookingapp.MainActivity;
import com.example.barberbookingapp.R;

/*
פרגמנט ההרשמה למערכת
בפרגמנט זה ממלאים פרטים בשביל ההרשמה הראשונית
שם משתמש, אימייל , טלפון, סיסמא ואימות של הסיסמא
כמו כו קיים ספינר בו בוחרים אם נרשמים למערכת בתור ספר או לקוח
אם נרשמים בתור ספר צריך להוסיף קוד סודי אשר ניתן לספרים בלבד כדי לאמת את זהותם
 */

public class RegisterFragment extends Fragment {
    private EditText usernameField, emailField, passwordField, confirmPasswordField, phoneField;
    private Button registerButton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        usernameField = view.findViewById(R.id.usernameRegister);
        emailField = view.findViewById(R.id.emailRegister);
        passwordField = view.findViewById(R.id.passwordRegister);
        confirmPasswordField = view.findViewById(R.id.confirmPasswordRegister);
        registerButton = view.findViewById(R.id.register_buttonRegister);
        Spinner roleSpinner = view.findViewById(R.id.roleSpinner);
        EditText secretCodeField = view.findViewById(R.id.secretCodeField);
        phoneField = view.findViewById(R.id.phoneField);



        //ספינר המציג שתי אופציות - ספר ולקוח
        //במידה והספינר נמצא על מצב של ספר אז נוספת תיבה למילוי הקוד הסודי לאימות
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = parent.getItemAtPosition(position).toString();
                if ("Barber".equals(selectedRole)) {
                    secretCodeField.setVisibility(View.VISIBLE);
                } else {
                    secretCodeField.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            String confirmPassword = confirmPasswordField.getText().toString();
            String username = usernameField.getText().toString();
            String selectedRole = roleSpinner.getSelectedItem().toString();
            String secretCode = secretCodeField.getText().toString();
            String phone = phoneField.getText().toString();

            //מוודא שהספר מזין את הקוד נכון
            //במידה ולא הוא לא יוכל להרשם למערכת בתור ספר
            //הקוד הסודי שאני בחרתי הוא 4799 ניתן לשינוי כל הזמן דרך הקוד בלבד
            if ("Barber".equals(selectedRole) && !"4799".equals(secretCode)) {
                Toast.makeText(getContext(), "Incorrect secret code", Toast.LENGTH_SHORT).show();
                return;
            }

            //קריאה לפונקציה של רישום משתמש חדש במיין עם הפרטים הרלוונטים לרישום שלו
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.registerUser( email,  password,  confirmPassword,  username, selectedRole,phone);
            //Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
        });


        return view;
    }
}