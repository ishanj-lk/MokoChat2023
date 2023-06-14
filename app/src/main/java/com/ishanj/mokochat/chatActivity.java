package com.ishanj.mokochat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

public class chatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ImageButton messageInputBtn;
    private TextView notFriendsTxt, profileName;
    private ImageView profileImage;

    private String uID, profileID;

    private FirebaseDatabase FBdatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Get uID with paper library
        Paper.init(chatActivity.this);
        uID = Paper.book().read("uID");

        //Get chat profileID
        profileID = getIntent().getStringExtra("profileID");
        Toast.makeText(chatActivity.this, profileID, Toast.LENGTH_SHORT).show();

        //Firebase Database
        FirebaseApp.initializeApp(chatActivity.this);
        FBdatabase = FirebaseDatabase.getInstance();

        //This initialize UI elements
        elementInitialize();
        //This triggers main actions
        actionTriggers();
    }

    private void elementInitialize() {
        messageInput = (EditText) findViewById(R.id.messageInput);
        messageInputBtn = (ImageButton) findViewById(R.id.messageInputBtn);
        notFriendsTxt = (TextView) findViewById(R.id.notFriendsTxt);
        profileName = (TextView) findViewById(R.id.profileName);
        profileImage = (ImageView) findViewById(R.id.profileImage);
    }

    private void actionTriggers() {
        fetchUserData();
        checkFriendshipState();
    }

    private void fetchUserData() {
    }

    private void checkFriendshipState() {
        DatabaseReference freindshipRef = FBdatabase.getReference("friends");
    }
}