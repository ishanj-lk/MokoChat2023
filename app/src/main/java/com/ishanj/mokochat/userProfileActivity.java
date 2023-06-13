package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class userProfileActivity extends AppCompatActivity {

    private Button userProfileActionBtn;
    private TextView userProfileName, userProfileCity;
    private ImageView userProfilePic;

    private String uID, profileID, userRelation = "";

    private FirebaseDatabase FBdatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Get uID with paper library
        Paper.init(userProfileActivity.this);
        uID = Paper.book().read("uID");

        //Get user state(new/existing)
        profileID = getIntent().getStringExtra("profileID");

        //Firebase Database
        FirebaseApp.initializeApp(userProfileActivity.this);
        FBdatabase = FirebaseDatabase.getInstance();

        //This initialize UI elements
        elementInitialize();
        //This will update users relation to us(me, friend, onRequest, notFriend)
        getUserRelation();
        //This triggers main actions
        actionTriggers();
        //Update User Info
        showUserInfo();
    }

    private void showUserInfo() {
        DatabaseReference userRef = FBdatabase.getReference("users");

        userRef.child(profileID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userName = snapshot.child("name").getValue().toString();
                    String homeTown = snapshot.child("homeTown").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileCity.setText(homeTown);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void getUserRelation() {
        if(uID.equals(profileID)){
            userRelation = "me";
            updateActionBtn();
        }
        else{
            DatabaseReference checkFriendRef = FBdatabase.getReference("friends");
            checkFriendRef.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.hasChild(profileID)){
                        userRelation = "friend";
                        updateActionBtn();
                    }
                    else{
                        checkProfileOnRequest();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateActionBtn() {
        String profileName = userProfileName.getText().toString();
        if(userRelation.equals("me")){
            userProfileActionBtn.setText("My Profile - Update User Info");
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserInfoIntent();
                }
            });
        }
        else if(userRelation.equals("friend")){
            userProfileActionBtn.setText("Unfriend this User");
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showActionDialog("Unfriend "+profileName,"Are you sure you want to unfriend "+profileName+"?, " +
                            "because this action can't undo. And all the chats and contacts with this user will removed.");
                }
            });
        }
        else if(userRelation.equals("onRequest")){
            userProfileActionBtn.setText("Cancel Friend Request");
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showActionDialog("Cancel Friend Request ","Are you sure you want to Cancel Friend Request?.");
                }
            });

        }
        else if(userRelation.equals("notFriend")){
            userProfileActionBtn.setText("Send a Friend Request");
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showActionDialog("Send Friend Request","Are you sure you want to send Friend Request?.");
                }
            });
        }
    }

    private void showActionDialog(String title, String body) {
        new AlertDialog.Builder(userProfileActivity.this)
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void updateUserInfoIntent() {
        Intent newUserIntent = new Intent(userProfileActivity.this, EditUserInfo.class);
        newUserIntent.putExtra("userState", "existing");
        startActivity(newUserIntent);
    }

    private void checkProfileOnRequest() {
        DatabaseReference checkOnRequestRef = FBdatabase.getReference("friends");
        checkOnRequestRef.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(profileID)){
                    userRelation = "onRequest";
                    updateActionBtn();
                }
                else{
                    userRelation= "notFriend";
                    updateActionBtn();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void actionTriggers() {
    }

    private void elementInitialize() {
        userProfileActionBtn = (Button) findViewById(R.id.userProfileActionBtn);
        userProfileName = (TextView) findViewById(R.id.userProfileName);
        userProfileCity = (TextView) findViewById(R.id.userProfileCity);
        userProfilePic = (ImageView) findViewById(R.id.userProfilePic);
    }
}