package com.example.barberbookingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberbookingapp.R;
import com.example.barberbookingapp.models.Appointments;

import java.util.List;

/*
אדפטר המיועד להצגת כל התורים של כל לקוח לעצמו בRecView
* של הלקוח כל תור יכלול תאריך ושעה וסוג השירות
וכפתור ביטול המאפשר לבטל את התור של הנבחר
* */


public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.AppointmentsViewHolder> {

    private final Context context;
    private final List<Appointments> appointmentsList;
    private final OnAppointmentCancelListener cancelListener;


    //בעת לחיצה על כפתור הביטול מודיע לפרגמנט של הלקוח שנמחק תור ושם מתבצעת פעולה המחיקה
    public interface OnAppointmentCancelListener {
        void onAppointmentCancel(Appointments appointment);
    }

    public AppointmentsAdapter(Context context, List<Appointments> appointmentsList, OnAppointmentCancelListener cancelListener) {
        this.context = context;
        this.appointmentsList = appointmentsList;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public AppointmentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentsViewHolder holder, int position) {
        //מציג את התאריך ושעה וסוג השירות על תור
        Appointments appointment = appointmentsList.get(position);
        holder.dateTimeText.setText(appointment.getDateTime());
        holder.serviceTypeText.setText(appointment.getServiceType());

        //קורא לביטול ומחיקת התור בעת לחיצה על כפתור CANCEL
        holder.cancelButton.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onAppointmentCancel(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }

    //השדות הסופיים שיהיו מוצגים לכל תור של הלקוח ב RECVIEW
    public static class AppointmentsViewHolder extends RecyclerView.ViewHolder {
        TextView dateTimeText, serviceTypeText;//, statusText;
        Button cancelButton;

        public AppointmentsViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTimeText = itemView.findViewById(R.id.appointment_date_time);
            serviceTypeText = itemView.findViewById(R.id.appointment_service_type);
            cancelButton = itemView.findViewById(R.id.cancel_appointment_button);
        }
    }
}
