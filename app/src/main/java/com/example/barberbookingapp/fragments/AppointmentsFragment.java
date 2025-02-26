package com.example.barberbookingapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.barberbookingapp.R;
import com.example.barberbookingapp.adapters.AppointmentsAdapter;
import com.example.barberbookingapp.models.Appointments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/*
פרגמנט זה מוצג לאחר לחיצה על כפתור צפיה בתורים של לקוח בפרגמנט הבית
בפרמגנט יש רשימה של כל התורים של של הלקוח המחובר
בכל תור מוצגים התאריך שעה סוג השירות
וכפתור ביטול לביטול התור הנבחר
* */

public class AppointmentsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AppointmentsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AppointmentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AppointmentsFragment newInstance(String param1, String param2) {
        AppointmentsFragment fragment = new AppointmentsFragment();
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
        View view = inflater.inflate(R.layout.fragment_appointments, container, false);

        //מקבל נתיב למשתמש בפיירבייס ולפי כך מציג את התורים שלו
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        appointmentsRef.orderByChild("clientId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Appointments> appointments = new ArrayList<>();
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    Appointments appointment = appointmentSnapshot.getValue(Appointments.class);
                    if (appointment != null) {
                        appointments.add(appointment);
                    }
                }

                // לאחר שהרשימה מוכנה מעדכן את הRECVIEW
                updateRecyclerView(appointments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }


            private void updateRecyclerView(List<Appointments> appointmentsList) {
                RecyclerView recyclerView = requireView().findViewById(R.id.appointments_recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                AppointmentsAdapter adapter = createAppointmentsAdapter(appointmentsList);
                recyclerView.setAdapter(adapter);
            }

            private AppointmentsAdapter createAppointmentsAdapter(List<Appointments> appointmentsList) {
                return new AppointmentsAdapter(requireContext(), appointmentsList, appointment -> {
                    cancelAppointment(appointment, appointmentsList);
                });
            }

            //פונקציה לביטול תור
            private void cancelAppointment(Appointments appointment, List<Appointments> appointmentsList) {
                //שינוי של הפורמט על מנת להגיע לנתיב הנכון של התור
                DatabaseReference appointmentRef = FirebaseDatabase.getInstance()
                        .getReference("appointments")
                        .child(appointment.getDateTime().replace(" ", "_").replace(":", "-"));

                //מחיקה מהפיירבייס ומהרשימה (במידה והמחיקה הצליחה)
                appointmentRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // הסרת התור מהרשימה
                        appointmentsList.remove(appointment);

                        // עדכון האדפטר
                        RecyclerView recyclerView = requireView().findViewById(R.id.appointments_recycler_view);
                        AppointmentsAdapter adapter = (AppointmentsAdapter) recyclerView.getAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }

                        // הצגת הודעת הצלחה למשתמש
                        Toast.makeText(requireContext(), "The appointment has been canceled! remove it from the calendar.", Toast.LENGTH_SHORT).show();
                    } else {
                        // טיפול במקרה של כשלון
                        Toast.makeText(requireContext(), "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }
}