package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

