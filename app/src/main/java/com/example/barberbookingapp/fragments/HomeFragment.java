package com.example.barberbookingapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barberbookingapp.MainActivity;
import com.example.barberbookingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/*
פרגמנט זה מוצג לאחר התחברות של לקוח למערכת
כותרת הפרגמנט מציגה את הכיתוב ומוסיפה את שם הלקוח הנוכחי
בפרגמנט קיים לוח מודעות אשר מתעדכן ישירות מעריכת הספר בפרגמנט של הספר
כמו כן, קיימת אופציה לבחירת תאריך שעה וסוג השירות ולאחר מכן קביעת התור
במידה והתאריך תפוס (יום חופש של המספרה) הלקוח יקבל על כך הודעה ולא יוכל לבחור את התאריך
כאשר לקוח יבחר שעה יוצגו לו רק השעות הזמינות בהן לא קיים תור באותו היום
בנוסף לכך, קיים בפרגמנט כפתור המוביל לפרגמנט שבו מוצגים כל התורים של הלקוח ושם יכול למחוק תור בעת הצורך
* */

public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        MainActivity mainActivity = (MainActivity) getActivity();

        //הצגת הכותרת של הפרגמנט
        //מציג את שם הלקוח המחובר ואיתו את ההודעה Welcome back
        TextView welcomeText = view.findViewById(R.id.welcome_text);
        if (mainActivity != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mainActivity.fetchUsername(userId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String username = task.getResult();
                    welcomeText.setText("Welcome back, " + username);
                } else {
                    welcomeText.setText("WELCOME Guest");
                }
            });
        }


        //מקבל הפניה לנתיב של לוח המודעות
        //במידה ויש הודעה יציג את ההודעה המעודכנת
        //במידה ואין לא יציג את הלוח
        TextView announcementText = view.findViewById(R.id.announcement_text);
        DatabaseReference announcementsRef = FirebaseDatabase.getInstance().getReference("announcements").child("current_announcement");

        announcementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String announcement = snapshot.getValue(String.class);
                if (announcement != null && !announcement.isEmpty()) {
                    announcementText.setText(announcement);
                    announcementText.setVisibility(View.VISIBLE);
                } else {
                    announcementText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        //מקבל את התוצאה של הספינר אשר מציג את סוגי השירותים הקיימים במספרה
        Spinner serviceSpinner = view.findViewById(R.id.service_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.barber_services,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);


        //בעת לחיצה על בחירת תאריך
        Button selectDateButton = view.findViewById(R.id.select_date_button);
        TextView selectedDateTimeText = view.findViewById(R.id.selected_datetime_text);

        selectDateButton.setOnClickListener(v -> {
            // פתיחת ה- DatePickerDialog לבחירת תאריך
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, year, month, dayOfMonth) -> {
                        // יצירת מחרוזת תאריך בפורמט YYYY-MM-DD
                        String selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year);

                        Spinner timeSpinner = view.findViewById(R.id.time_spinner);
                        selectedDateTimeText.setText(selectedDate);

                        DatabaseReference holidaysRef = FirebaseDatabase.getInstance().getReference("holidays");

                        // בדיקה אם התאריך נמצא ברשימת ימי החופש
                        holidaysRef.child(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // אם התאריך נמצא בימי החופש, מציגים הודעה וחוסמים את בחירת כל שעה
                                    Toast.makeText(requireContext(), "The barbershop is closed on this date.", Toast.LENGTH_LONG).show();
                                    timeSpinner.setAdapter(null); // מנקה את ה-Spinner של השעות
                                } else {
                                    // אם התאריך לא יום חופש – טוענים שעות פנויות כרגיל
                                    loadAvailableTimes(selectedDate, timeSpinner);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(requireContext(), "Failed to check holiday dates", Toast.LENGTH_SHORT).show();
                            }
                        });
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });



        Button viewAppointmentsButton = view.findViewById(R.id.view_appointments_button);
        viewAppointmentsButton.setOnClickListener(v -> {
            // פעולה שתפתח את פרגמנט הפגישות
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_appointmentsFragment);
        });



        Button bookAppointmentButton = view.findViewById(R.id.book_appointment_button);

        Spinner timeSpinner = view.findViewById(R.id.time_spinner);
        bookAppointmentButton.setOnClickListener(v -> {
            // קבלת השירות שנבחר מהספינר
            String selectedService = (String) serviceSpinner.getSelectedItem();

            String selectedTime = timeSpinner.getSelectedItem().toString();
            // קבלת התאריך והשעה שנבחרו מה-TextView
            String selectedDateTime = selectedDateTimeText.getText().toString();
            selectedDateTime = selectedDateTime + " " + selectedTime;

            // בדיקה האם ערכים נבחרו
            if (selectedService != null && !selectedDateTime.equals("No date selected")) {
                // קריאה לפונקציה במיין אקטיביטי לשמירת התור
                if (mainActivity != null) {
                    mainActivity.bookAppointment(selectedService, selectedDateTime);
                }
            } else {
                // הצגת הודעה למשתמש אם אין בחירה
                Toast.makeText(getContext(), "Please select a service and a date/time.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    //בדיקה של השעות בתאריך הנבחר כדי לדעת מה השעות הפנויות ולהציג אותן בספינר
    private void loadAvailableTimes(String selectedDate, Spinner timeSpinner) {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // רשימה של כל השעות בין 08:00 ל-20:00 עם מרווח של חצי שעה
                List<String> allSlots = new ArrayList<>();
                for (int hour = 8; hour < 20; hour++) {
                    allSlots.add(String.format("%02d:00", hour));
                    allSlots.add(String.format("%02d:30", hour));
                }

                // רשימת השעות התפוסות מתוך הפיירבייס
                List<String> bookedSlots = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String dateTime = childSnapshot.child("dateTime").getValue(String.class);
                    if (dateTime != null && dateTime.startsWith(selectedDate)) {
                        String[] parts = dateTime.split(" ");
                        if (parts.length == 2) {
                            bookedSlots.add(parts[1]); // רק השעה
                        }
                    }
                }

                // סינון שעות פנויות בלבד
                List<String> availableSlots = new ArrayList<>();
                for (String slot : allSlots) {
                    if (!bookedSlots.contains(slot)) {
                        availableSlots.add(slot);
                    }
                }

                // עדכון הספינר עם השעות הפנויות
                ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        availableSlots
                );
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                timeSpinner.setAdapter(timeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load available times", Toast.LENGTH_SHORT).show();
            }
        });
    }

}