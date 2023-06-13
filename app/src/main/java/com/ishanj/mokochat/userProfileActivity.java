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
import java.util.Map;

import io.paperdb.Paper;

public class userProfileActivity extends AppCompatActivity {

    private Button userProfileActionBtn, userProfileAcceptBtn;
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
        //This will update users relation to us(me, friend, onRequestSend, onRequestReceived, notFriend)
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
                        checkProfileOnRequestSend();
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
        else if(userRelation.equals("onRequestSend")){
            userProfileActionBtn.setText("Cancel Friend Request");
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showActionDialog("Cancel Sent Friend Request ","Are you sure you want to Cancel Friend Request?.");
                }
            });

        }
        else if(userRelation.equals("onRequestReceived")){
            userProfileActionBtn.setText("Delete Friend Request");
            userProfileAcceptBtn.setVisibility(View.VISIBLE);
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptFriendRequest();
                }
            });
            userProfileActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showActionDialog("Cancel Received Friend Request ","Are you sure you want to Delete Friend Request?.");
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
                        if(userRelation.equals("friend")){
                            unfriendUser();
                        }
                        else if(userRelation.equals("onRequestSend")){
                            sentRequestCancel();

                        }
                        else if(userRelation.equals("onRequestReceive")){
                            receivedRequestCancel();
                        }
                        else if(userRelation.equals("notFriend")){
                            sendFriendRequest();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void acceptFriendRequest() {//not complete
        DatabaseReference acceptRef = FBdatabase.getReference("friends");
        Map<String, Object> acceptData = new HashMap<>();
        acceptData.put(profileID,"");
        acceptRef.child(uID).setValue(acceptData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }

    private void sendFriendRequest() {
        DatabaseReference sendRequestRef = FBdatabase.getReference("requestSend");
        Map<String, Object> sendRequestData = new HashMap<>();
        sendRequestData.put(profileID,"");
        sendRequestRef.child(uID).setValue(sendRequestData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    DatabaseReference receiveRequestRef = FBdatabase.getReference("requestReceive");
                    Map<String, Object> receiveRequestData = new HashMap<>();
                    receiveRequestData.put(uID,"");
                    receiveRequestRef.child(profileID).setValue(receiveRequestData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(userProfileActivity.this, "Friend Request Sent Successfully...", Toast.LENGTH_SHORT).show();
                                Intent mainUIIntent = new Intent(userProfileActivity.this, MainInterface.class);
                                startActivity(mainUIIntent);
                            }
                        }
                    });
                }
            }
        });
    } //complete

    private void receivedRequestCancel() {
    }

    private void sentRequestCancel() {
        DatabaseReference sendRequestRef = FBdatabase.getReference("requestSend");
        DatabaseReference receiveRequestRef = FBdatabase.getReference("requestReceive");
        sendRequestRef.child(uID).child(profileID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    receiveRequestRef.child(profileID).child(uID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(userProfileActivity.this, "Friend Request Cancel Successfully...", Toast.LENGTH_SHORT).show();
                                Intent mainUIIntent = new Intent(userProfileActivity.this, MainInterface.class);
                                startActivity(mainUIIntent);
                            }
                        }
                    });
                }
            }
        });

    }

    private void unfriendUser() {
    }


    private void updateUserInfoIntent() {
        Intent newUserIntent = new Intent(userProfileActivity.this, EditUserInfo.class);
        newUserIntent.putExtra("userState", "existing");
        startActivity(newUserIntent);
    }

    private void checkProfileOnRequestSend() {
        DatabaseReference checkOnRequestSendRef = FBdatabase.getReference("requestSend");
        checkOnRequestSendRef.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(profileID)){
                    userRelation = "onRequestSend";
                    updateActionBtn();
                }
                else{
                    checkProfileOnRequestReceived();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkProfileOnRequestReceived() {
        DatabaseReference checkOnRequestReceivedRef = FBdatabase.getReference("requestReceive");
        checkOnRequestReceivedRef.child(uID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(profileID)){
                    userRelation = "onRequestReceived";
                    updateActionBtn();
                }
                else{
                    userRelation = "notFriend";
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
        userProfileAcceptBtn = (Button) findViewById(R.id.userProfileAcceptBtn);
        userProfileName = (TextView) findViewById(R.id.userProfileName);
        userProfileCity = (TextView) findViewById(R.id.userProfileCity);
        userProfilePic = (ImageView) findViewById(R.id.userProfilePic);
    }
}