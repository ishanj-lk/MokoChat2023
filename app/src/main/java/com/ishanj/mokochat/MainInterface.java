package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainInterface extends AppCompatActivity {

    private String uID;
    private Button btnLogout, btnUpdateUserInfoBtn;
    private FirebaseDatabase FBdatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);

        //Get uID with paper library
        Paper.init(MainInterface.this);
        uID = Paper.book().read("uID");

        //Firebase Database
        FirebaseApp.initializeApp(MainInterface.this);
        FBdatabase = FirebaseDatabase.getInstance();

        //Check for new user
        newUserCheck();
        //This initialize UI elements
        elementInitialize();
        //This triggers main actions
        actionTriggers();
        //Fetch Sample data
        fetchList();


    }

    private void fetchList() {
        DatabaseReference listRef = FBdatabase.getReference("texts");
        // Retrieve the data from Firebase Realtime Database
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinearLayout linearLayout = findViewById(R.id.linearLayout); // Replace with your actual layout container
                linearLayout.removeAllViews(); // Clear the existing views

                TextView originalTextView = findViewById(R.id.itemTextView);

                int idCounter=1;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // Process each child snapshot and retrieve the data
                    String value = childSnapshot.getValue(String.class);

//                    // Create a new TextView for each item
//                    TextView textView = new TextView(MainInterface.this); // Replace MainActivity with your activity or use 'getContext()' in a fragment
//                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
//                    textView.setText(value);
//
//                    // Add the TextView to the layout container
//                    linearLayout.addView(textView);
                    TextView textViewCopy = new TextView(MainInterface.this); // Replace MainActivity with your activity or use 'getContext()' in a fragment
                    textViewCopy.setLayoutParams(originalTextView.getLayoutParams()); // Set the layout params of the original TextView
                    textViewCopy.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextView.getTextSize());
                    textViewCopy.setText(value);

                    textViewCopy.setId(idCounter); // Assign a unique ID to the TextView
                    textViewCopy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int textViewId = v.getId();
                            // Handle the click event and show the ID
                            Toast.makeText(MainInterface.this, "Clicked TextView ID: " + textViewId, Toast.LENGTH_SHORT).show();
                        }
                    });

                    // Add the copied TextView to the layout container
                    linearLayout.addView(textViewCopy);

                    idCounter++; // Increment the ID counter
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data", databaseError.toException());
            }
        });
    }

    private void newUserCheck() {
        DatabaseReference userRef = FBdatabase.getReference("users");

        userRef.child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Intent newUserIntent = new Intent(MainInterface.this, EditUserInfo.class);
                    newUserIntent.putExtra("userState", "new");
                    startActivity(newUserIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void actionTriggers() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogOut();

            }
        });
        btnUpdateUserInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newUserIntent = new Intent(MainInterface.this, EditUserInfo.class);
                newUserIntent.putExtra("userState", "existing");
                startActivity(newUserIntent);
            }
        });
    }

    private void userLogOut() {
        Intent logoutIntent = new Intent(MainInterface.this, MainActivity.class);
        startActivity(logoutIntent);
        Paper.book().delete("uID");
    }

    private void elementInitialize() {
        btnLogout = (Button) findViewById(R.id.logout);
        btnUpdateUserInfoBtn = (Button) findViewById(R.id.updateUserInfoBtn);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}

