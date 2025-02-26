package com.example.barberbookingapp.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.barberbookingapp.R;
import com.example.barberbookingapp.adapters.HolidaysAdapter;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



/*
פרגמנט זה מוצג לאחר לחיצה על כפתור ניהול ימי חופש בפרגמנט של הספרים
בפרמגנט יש רשימה של כל ימי החופש של המספרה
כל יום חופש מוצג עם תאריך וכפתור מחיקה שלו
כמו כן, קיים כפתור להוספה של יום חופש לרשימה
* */



public class HolidaysFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyText;
    private List<String> holidaysList;
    private HolidaysAdapter adapter;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_holidays, container, false);

        recyclerView = view.findViewById(R.id.holidays_recycler_view);
        emptyText = view.findViewById(R.id.empty_holidays_text);
        Button addHolidayButton = view.findViewById(R.id.add_holiday_button);

        holidaysList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("holidays");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HolidaysAdapter(holidaysList, databaseReference);
        recyclerView.setAdapter(adapter);

        loadHolidays();

        //בעת לחיצה על כפתור הוספת יום חופש תתבצע קריאה לפונקציה המבצעת
        addHolidayButton.setOnClickListener(v -> openDatePicker());

        return view;
    }

    //פונקציה המוסיפה יום חופש לרשימה
    //פותחת לוח שנה ובו בחירת תאריך ליום החופש ומוסיפה בפורמט הרצוי
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format("%02d-%02d-%d", dayOfMonth, month + 1, year);
                    databaseReference.child(selectedDate).setValue(true);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    //טעינת כל ימי החופש והצגתם המעודכנת
    private void loadHolidays() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holidaysList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    holidaysList.add(data.getKey());
                }
                adapter.notifyDataSetChanged();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //פונקציה שעוזרת להציג את מה שצריך בהתאם למצב הרשימה
    private void updateUI() {
        if (holidaysList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
