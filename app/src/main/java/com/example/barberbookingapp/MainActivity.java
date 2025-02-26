package com.example.barberbookingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.barberbookingapp.models.Appointments;
import com.example.barberbookingapp.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
קובץ המיין של האפליקציה
מוגדרות בו פונקציות אשר נקראות מהפרגמנטים
פונקצית התחברות
פונקצית הרשמה
פונקציה של הוספת תור
פונקציה של הצגת שם המשתמש
ועוד פונקציות הקשורות לינווט של הפרגמנטים
*/

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();

    }
    private FirebaseAuth mAuth;


    //פונקציה של קביעת תור ללקוח
    public void bookAppointment(String serviceType, String dateTime) {
        // מקבל את המפתח של המשתמש הנוכחי
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Appointments appointment = new Appointments(serviceType, userId, "pending", dateTime);

        //יוצר נתיב אל הפיירבייס
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");

        // מפתח ייחודי לתור
        String appointmentKey = dateTime.replace(" ", "_").replace(":", "-");

        // שמירת האובייקט בפיירבייס והצגת הערה
        appointmentsRef.child(appointmentKey).setValue(appointment)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Appointment scheduled and added to calendar.", Toast.LENGTH_SHORT).show();
                        Log.d("datetime", "date time is: " + dateTime.toString());
                        //  המרת התאריך והזמן למילישניות כדי שאוכל להוסיף לקלנדר
                        long startTime = convertDateTimeToMillis(dateTime);
                        long endTime = startTime + 3600000; // משך של שעה

                        // הוספת האירוע ללוח השנה
                        addAppointmentToCalendar(serviceType, "barbershop", startTime, endTime);
                    } else {
                        Toast.makeText(this, "Failed to book appointment", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    //פונקציות להוספת האירוע לקלנדר
    private void addAppointmentToCalendar(String title, String location, long startTime, long endTime) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR}, 100);
            return;
        }

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.EVENT_LOCATION, location);
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        if (uri != null) {
            //Toast.makeText(this, "Appointment added to the calendar", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("CalendarDebug", "Failed to add event to calendar");
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No permission to modify the calendar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private long convertDateTimeToMillis(String dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());

        try {
            Date date = sdf.parse(dateTime);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.setTimeZone(TimeZone.getDefault());

                Log.d("DateTimeConversion", "Parsed Date: " + calendar.getTime().toString());
                return calendar.getTimeInMillis();
            } else {
                Log.e("DateTimeConversion", "Failed to parse date");
                return System.currentTimeMillis();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("DateTimeConversion", "ParseException: " + e.getMessage());
            return System.currentTimeMillis();
        }
    }





    //פונקציה אשר מקבלת את המפתח של המשתמש ומחזירה את השם משתמש
    //במידה ולא נמצא שם משתמש תחחיר GUEST
    public Task<String> fetchUsername(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    taskCompletionSource.setResult(dataSnapshot.getValue(String.class));
                } else {
                    taskCompletionSource.setResult("Guest");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }


    //פונקציה של התחברות המשתמש
    //מקבלת אימייל וסיסמא ובודקת בפיירבייס האם המשתמש קיים
    //במידה וקיים בודקת לפי הROLE האם הוא ספר או לקוח ומפנה אותו לפרגמנט המתאים
    public void loginUser(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                            //בדיקת הROLE
                            userRef.child("role").get().addOnCompleteListener(roleTask -> {
                                if (roleTask.isSuccessful()) {
                                    DataSnapshot dataSnapshot = roleTask.getResult();
                                    if (dataSnapshot.exists()) {
                                        String role = dataSnapshot.getValue(String.class);
                                        if ("Barber".equals(role)) {
                                            navigateToBarberFragment();
                                        } else {
                                            navigateToHomeFragment();
                                        }
                                    } else {
                                        // אם ROLE לא קיים
                                        Toast.makeText(this, "Role not found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // טיפול בשגיאה
                                    Toast.makeText(this, "Failed to fetch role", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    //פונקציות ניווט לפרגמנטים המתאימים
    private void navigateToBarberFragment() {
        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        navController.navigate(R.id.action_loginFragment_to_adminFragment);
    }

    private void navigateToHomeFragment() {
        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        navController.navigate(R.id.action_loginFragment_to_homeFragment);
    }

    private void navigateToLoginFragment() {
        NavController navController = Navigation.findNavController(this, R.id.fragmentContainerView);
        navController.navigate(R.id.action_registerFragment_to_loginFragment);
    }


    //פונקצית הרשמה של משתמש חדש
    public void registerUser(String email, String password, String confirmPassword, String username, String role, String phone) {
        // בדיקה אם כל השדות מלאים
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקת אימות סיסמאות
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // במידה והכל תקין יוצרים משתמש ושומרים אותו בפיירבייס
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            User newUser = new User(username, email, role, phone);

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(userId)
                                    .setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                        navigateToLoginFragment();
                                    })
                                        .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}