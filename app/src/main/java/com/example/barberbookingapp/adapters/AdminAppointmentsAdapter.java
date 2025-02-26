package com.example.barberbookingapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberbookingapp.R;
import com.example.barberbookingapp.models.Appointments;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/*
אדפטר המיועד להצגת כל התורים של הלקוחות באופן כולל בRecView
* של האדמין כל תור יכלול תאריך ושעה, סוג השירות, שם המשתמש, אימייל, טלפון
וכפתור ביטול המאפשר לבטל את התור של המשתמש
* */


public class AdminAppointmentsAdapter extends RecyclerView.Adapter<AdminAppointmentsAdapter.AppointmentViewHolder> {

    private final Context context;
    private final List<Appointments> appointments;

    public AdminAppointmentsAdapter(Context context, List<Appointments> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointments appointment = appointments.get(position);

        // הצגת תאריך שעה וסוג השירות - המידע נלקח מהפרגמנט ישירות לפי בחירת הלקוח
        holder.dateTimeText.setText(appointment.getDateTime());
        holder.serviceTypeText.setText(appointment.getServiceType());

        //מחיקת התור הנבחר מהפיירבייס בעת לחיצה על Cancel
        holder.cancelButton.setOnClickListener(v -> {
            // מחיקת התור מהפיירבייס - החלפה לפורמט המתאים כדי להגיע לנתיב נכון
            DatabaseReference appointmentRef = FirebaseDatabase.getInstance()
                    .getReference("appointments")
                    .child(appointment.getDateTime().replace(" ", "_").replace(":", "-"));
            Log.d("FirebasePath", "Path to delete: " + appointmentRef.toString());


            appointmentRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // מחיקה מהפיירבייס הצליחה - מוחק ומעדכן גם את הRECVIEW
                    appointments.remove(appointment);
                    notifyItemRemoved(holder.getAdapterPosition());
                    Toast.makeText(context, "canceled! Please notify the client.", Toast.LENGTH_SHORT).show();
                } else {
                    // טיפול במקרה של כשלון
                    Toast.makeText(context, "Failed to cancel appointment", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // קבלת ה-clientId מתוך APPOINTMENTS
        String clientId = appointment.getClientId();
        if (clientId != null && !clientId.isEmpty()) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(clientId);

            //  קריאה והצגה של נתוני המשתמש מתוך USERS
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // עדכון ערכי המשתמש בתור
                    String username = snapshot.child("username").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    holder.userName.setText(username != null ? username : "N/A");
                    holder.userPhone.setText(phone != null ? phone : "N/A");
                    holder.userEmail.setText(email != null ? email : "N/A");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            //אם לא נמצאו הפרטים נציג UNKNOWN על החלק הרלוונטי בתור
            holder.userName.setText("Unknown");
            holder.userPhone.setText("Unknown");
            holder.userEmail.setText("Unknown");
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }


    //המידע שנשלח להצגת התור בRECVIEW
    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeText;
        TextView serviceTypeText;
        TextView userName;
        TextView userPhone;
        TextView userEmail;
        Button cancelButton;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeText = itemView.findViewById(R.id.appointment_date_time);
            serviceTypeText = itemView.findViewById(R.id.appointment_service_type);
            userName = itemView.findViewById(R.id.user_name);
            userPhone = itemView.findViewById(R.id.user_phone);
            userEmail = itemView.findViewById(R.id.user_email);
            cancelButton = itemView.findViewById(R.id.cancel_appointment_button);
        }
    }
}
