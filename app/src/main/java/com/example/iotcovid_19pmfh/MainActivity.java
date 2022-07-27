package com.example.iotcovid_19pmfh;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "Notification";
    String[] user = {"User1", "User2", "User3", "User4"};
    TextView temp, spo2, rate, status;
    Button btnTograph, refresh;
    ImageView icon;
    int spo, heart;
    double tem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //notification channel
        NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        //top bar title
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("Firebase");

        temp = findViewById(R.id.temp);
        spo2 = findViewById(R.id.spo2);
        rate = findViewById(R.id.rate);
        status = findViewById(R.id.status);
        icon = findViewById(R.id.icon);
        refresh = findViewById(R.id.button1);
        btnTograph = findViewById(R.id.button2);

        //notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_danger)
                .setContentTitle("Low oxygen level!")
                .setContentText("Please go to nearest medical centre for check-up")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        btnTograph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, graph.class));
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name(dbRef);

            }

        });

        name(dbRef);

    }

    private void name(DatabaseReference dbRef) {
        dbRef.child("User1").child("heartrate").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    rate.setText(String.valueOf(task.getResult().getValue()));

                    dbRef.child("User1").child("spo2").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                spo2.setText(String.valueOf(task.getResult().getValue()));

                                dbRef.child("User1").child("temperature").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        } else {
                                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                            temp.setText(String.valueOf(task.getResult().getValue()));


                                            try {
                                                spo = Integer.parseInt(spo2.getText().toString().trim());
                                                tem = Double.parseDouble(temp.getText().toString().trim());
                                                heart = Integer.parseInt(rate.getText().toString().trim());

                                                if (spo <= 94) {
                                                    status.setText("Danger");
                                                    icon.setImageResource(R.drawable.ic_danger);
                                                    //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                                                    //notificationManager.notify(1, builder.build());
                                                } else {
                                                    if ((heart > 60 && heart < 100) && (tem >= 36 && tem < 38)) {
                                                        status.setText("Good");
                                                    }
                                                    else {
                                                        status.setText("Caution");
                                                    }

                                                }
                                            } catch (Exception e) {
                                                return;
                                            }


                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

}