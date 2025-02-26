package com.example.barberbookingapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberbookingapp.R;
import com.example.barberbookingapp.adapters.AdminAppointmentsAdapter;
import com.example.barberbookingapp.models.Appointments;
import com.example.barberbookingapp.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*
פרגמנט זה מוצג לאחר התחברות של ספר למערכת
בפרמגנט יש רשימה של כל התורים של כלל הלקוחות
כמו כן, כפתור לעדכון ימי חופש למספרה והצגתם
כפתור לעדכון מלאי הציוד במספרה והצגתו
כפתור הנותן גישה לביצוע עריכה של לוח המודעות המוצג ללקוחות בעת התחברות למערכת
* */

public class AdminFragment extends Fragment {

    private RecyclerView adminAppointmentsRecyclerView;
    private AdminAppointmentsAdapter adminAppointmentsAdapter;
    private List<Appointments> appointmentsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        adminAppointmentsRecyclerView = view.findViewById(R.id.admin_appointments_recycler_view);
        appointmentsList = new ArrayList<>();
        adminAppointmentsAdapter = new AdminAppointmentsAdapter(requireContext(), appointmentsList);

        adminAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adminAppointmentsRecyclerView.setAdapter(adminAppointmentsAdapter);

        //בעת לחיצה על כפתור ימי החופש של המספרה הספר ינווט לפרגמנט הרלוונטי
        Button HolidaysFragmentButton = view.findViewById(R.id.manage_holidays_button);
        HolidaysFragmentButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_adminFragment_to_holidaysFragment);
        });


        //בעת לחיצה על כפתור מלאי הציוד של המספרה הספר ינווט לפרגמנט הרלוונטי
        Button inventoryButton = view.findViewById(R.id.inventory_button);
        inventoryButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_adminFragment_to_inventoryFragment);
        });


        //בעת לחיצה על כפתור עריכת הודעה יפתח דיאלוג ובו כבר מופיעה ההודעה הנוכחית
        //כך הספר יכול לעדכן אותה מתי שירצה
        //ההודעה שמורה בפיירבייס
        Button postAnnouncementButton = view.findViewById(R.id.post_announcement_button);
        postAnnouncementButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Edit Announcement");

            // יצירת View מותאם אישית לדיאלוג
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_announcement, null);
            EditText announcementEditText = dialogView.findViewById(R.id.announcement_text_input);

            // רפרנס להודעה - קיימת רק אחת
            DatabaseReference announcementsRef = FirebaseDatabase.getInstance().getReference("announcements").child("current_announcement");

            // קריאת ההודעה הנוכחית והצגתה בדיאלוג
            announcementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String existingText = snapshot.getValue(String.class);
                    if (existingText != null) {
                        announcementEditText.setText(existingText);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(), "Failed to load announcement", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setView(dialogView);

            // כפתור עדכון ההודעה
            builder.setPositiveButton("Update", (dialog, which) -> {
                String updatedText = announcementEditText.getText().toString().trim();
                if (!updatedText.isEmpty()) {
                    // עדכון ההודעה בפיירבייס
                    //לפי הטקסט המעודכן שומרים את ההודעה החדשה בפיירבייס במקום ההודעה הקודמת (לא בנוסף)
                    announcementsRef.setValue(updatedText)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Announcement updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to update announcement", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid announcement", Toast.LENGTH_SHORT).show();
                }
            });

            // (סוגר את הדיאלוג) כפתור ביטול
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        });

        loadAppointments();

        return view;
    }


    //טעינת על הפגישות של כל הלקוחות מהפיירבייס
    private void loadAppointments() {
        //יצירת נתיב לפגישות הלקוחות
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentsList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Appointments appointment = child.getValue(Appointments.class);
                    if (appointment != null) {
                        appointmentsList.add(appointment);
                    }
                }
                //לאחר הטעינה של התורים העדכניים מציג אותם בRECVIEW
                adminAppointmentsAdapter.notifyDataSetChanged();
            }


            //אם קריאת הנתונים נכשלה יציג הודעת שגיאה
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
