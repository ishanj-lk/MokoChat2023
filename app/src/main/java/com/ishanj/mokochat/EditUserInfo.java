package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.paperdb.Paper;

public class EditUserInfo extends AppCompatActivity {

    private EditText editUserInfoNameGet, editUserInfoTownGet;
    private String editUserInfoNameGetValue, editUserInfoTownGeValue;
    private Button editUserInfoUpdateBtnGet;
    private String userState, uID;
    private FirebaseDatabase FBdatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        //Get user state(new/existing)
        userState = getIntent().getStringExtra("userState");

        //Get uID with paper library
        Paper.init(EditUserInfo.this);
        uID = Paper.book().read("uID");

        //Firebase Database
        FirebaseApp.initializeApp(EditUserInfo.this);
        FBdatabase = FirebaseDatabase.getInstance();

        //This initialize UI elements
        elementInitialize();
        //This triggers main actions
        actionTriggers();
        //If existing user show user info
        showUserInfo();
    }

    private void actionTriggers() {
        editUserInfoUpdateBtnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserInfoNameGetValue = editUserInfoNameGet.getText().toString();
                editUserInfoTownGeValue = editUserInfoTownGet.getText().toString();
                if(TextUtils.isEmpty(editUserInfoNameGetValue) || TextUtils.isEmpty(editUserInfoTownGeValue)){
                    Toast.makeText(EditUserInfo.this, "Please fill all fields...", Toast.LENGTH_SHORT).show();
                }
                else{
                    updateUser();
                }
            }
        });
    }

    private void showUserInfo() {
        if(userState.equals("existing")){
            retrieveUserData();
        }
    }

    private void retrieveUserData() {
        DatabaseReference userRef = FBdatabase.getReference("users");

        userRef.child(uID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userName = snapshot.child("name").getValue().toString();
                    String homeTown = snapshot.child("homeTown").getValue().toString();
                    editUserInfoNameGet.setText(userName);
                    editUserInfoTownGet.setText(homeTown);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateUser() {
        DatabaseReference registerUserRef = FBdatabase.getReference("users").child(uID);
        Map<String, Object> registerUserData = new HashMap<>();
        registerUserData.put("name", editUserInfoNameGetValue);
        registerUserData.put("homeTown", editUserInfoTownGeValue);
        registerUserData.put("name4search", editUserInfoNameGetValue.toLowerCase().replace(" ", ""));
        registerUserRef.setValue(registerUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(EditUserInfo.this, "User Information Updated Successfully...", Toast.LENGTH_SHORT).show();
                    Intent mainUIIntent = new Intent(EditUserInfo.this, MainInterface.class);
                    startActivity(mainUIIntent);
                }
            }
        });
    }

    private void elementInitialize() {
        editUserInfoNameGet = (EditText) findViewById(R.id.editUserInfoName);
        editUserInfoTownGet = (EditText) findViewById(R.id.editUserInfoTown);

        editUserInfoUpdateBtnGet = (Button) findViewById(R.id.editUserInfoUpdateBtn);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}