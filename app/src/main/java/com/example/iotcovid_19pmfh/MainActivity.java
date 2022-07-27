package com.example.iotcovid_19pmfh;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_ONE_TAP = 2;
    private static final String TAG = "Tag-name";
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    String[] user = {"User1", "User2", "User3", "User4"};
    TextView temp, spo2, rate, status;
    Button btnTograph, refresh;
    int spo, tem, heart;
    //Spinner spin = (Spinner) findViewById(R.id.spinner);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = database.getReference("Firebase");

        temp = findViewById(R.id.temp);
        spo2 = findViewById(R.id.spo2);
        rate = findViewById(R.id.rate);
        status = findViewById(R.id.status);
        refresh = findViewById(R.id.button1);
        btnTograph = findViewById(R.id.button2);

        //spin.setOnItemSelectedListener(this);
        //ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, user);
        //spin.setAdapter(aa);

        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            startIntentSenderForResult(
                                    result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });

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
                                                tem = Integer.parseInt(temp.getText().toString().trim());
                                                heart = Integer.parseInt(rate.getText().toString().trim());

                                                if (spo <= 94) {
                                                    status.setText("Danger");
                                                    //rate.setText((CharSequence) spo2);
                                                } else {
                                                    status.setText("Good");
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