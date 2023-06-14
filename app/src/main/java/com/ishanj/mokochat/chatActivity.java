package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.paperdb.Paper;

public class chatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ImageButton messageInputBtn, gotoBottomBtn;
    private TextView notFriendsTxt, profileName, senderMessagesDateTime, myMessagesDateTime;
    private ImageView profileImage;
    private RelativeLayout messageInputLayout;
    private LinearLayout chatsLinearLayout;
    private ScrollView chatScroller;

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
        messageInputLayout = (RelativeLayout) findViewById(R.id.messageInputLayout);
        chatsLinearLayout = (LinearLayout) findViewById(R.id.chatsLinearLayout);
        chatScroller = (ScrollView) findViewById(R.id.chatScroller);
        gotoBottomBtn = (ImageButton) findViewById(R.id.gotoBottomBtn);
        senderMessagesDateTime = (TextView) findViewById(R.id.senderMessagesDateTime);
        myMessagesDateTime = (TextView) findViewById(R.id.myMessagesDateTime);
    }

    private void actionTriggers() {
        fetchUserData();
        checkFriendshipState();
        messageInputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        fetchMessages();
        gotoBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatScroller.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        showScrollBtn();
    }

    private void showScrollBtn() {
        chatScroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (chatScroller.getChildAt(0).getBottom() <= (chatScroller.getHeight() + chatScroller.getScrollY())) {
                    // Reached the bottom of the ScrollView
                    gotoBottomBtn.setVisibility(View.GONE);
                } else {
                    // Not at the bottom of the ScrollView
                    gotoBottomBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void sendMessage() {
        if(!TextUtils.isEmpty(messageInput.getText().toString())){
            String message = messageInput.getText().toString();
            UUID uuid = UUID.randomUUID();
            String uniqueID = uuid.toString();

            // Get the current date and time
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();

            // Get the date and time separately
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // 0-based
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            long timestamp = (2000000000-(System.currentTimeMillis()/100));

            DatabaseReference sendMsgRef = FBdatabase.getReference("chats");
            Map<String, Object> msgData = new HashMap<>();
            msgData.put("id",uniqueID);
            msgData.put("message",message);
            msgData.put("time",hour + ":" + minute + ":" + second);
            msgData.put("date",year + "-" + month + "-" + day);
            msgData.put("deleteState","notDeleted");
            msgData.put("readState", "notRead");
            msgData.put("owner",uID);
            msgData.put("timestamp",-timestamp);


            sendMsgRef.child(uID).child(profileID).child(uniqueID).setValue(msgData).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        sendMsgRef.child(profileID).child(uID).child(uniqueID).setValue(msgData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                setPriority();
                            }
                        });
                    }
                }
            });
        }
        else {
            Toast.makeText(chatActivity.this, "Please enter the message first to send message...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setPriority() {
        long timestamp = (2000000000-(System.currentTimeMillis()/100));
        DatabaseReference priorityRef = FBdatabase.getReference("priority");
        Map<String, Object> priorityData = new HashMap<>();
        priorityData.put("timestamp",timestamp);
        priorityRef.child(uID).child(profileID).setValue(priorityData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    priorityRef.child(profileID).child(uID).setValue(priorityData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                messageInput.setText("");
                                chatScroller.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        }
                    });
                }
            }
        });
    }

    private void fetchUserData() {
        DatabaseReference userDetailsRef = FBdatabase.getReference("users");
        userDetailsRef.child(profileID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String profileNameGet = snapshot.child("name").getValue().toString();
                    //String profilePicLinkGet = snapshot.child("imageUrl").getValue().toString();

                    profileName.setText(profileNameGet);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFriendshipState() {
        DatabaseReference friendshipRef = FBdatabase.getReference("friends");
        friendshipRef.child(uID).child(profileID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    notFriendsTxt.setVisibility(View.GONE);
                    messageInputLayout.setVisibility(View.VISIBLE);
                }
                else{
                    notFriendsTxt.setVisibility(View.VISIBLE);
                    messageInputLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchMessages() {

        DatabaseReference chatsRef = FBdatabase.getReference("chats");
        chatsRef.child(uID).child(profileID).orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsLinearLayout.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(chatActivity.this);

                for (DataSnapshot child : snapshot.getChildren()) {
                    View itemLayout = inflater.inflate(R.layout.message_item_layout, chatsLinearLayout, false);
                    TextView senderMessages = itemLayout.findViewById(R.id.senderMessages);
                    TextView myMessages = itemLayout.findViewById(R.id.myMessages);
                    TextView senderMessagesDateTime = itemLayout.findViewById(R.id.senderMessagesDateTime);
                    TextView myMessagesDateTime = itemLayout.findViewById(R.id.myMessagesDateTime);

                    String childID = child.getKey();
                    String owner = child.child("owner").getValue().toString();
                    String message = child.child("message").getValue().toString();
                    String dateGet = child.child("date").getValue().toString();
                    String time = child.child("time").getValue().toString();

                    // Get the current date and time
                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();

                    // Get the date and time separately
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1; // 0-based
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    if(dateGet.equals(year + "-" + month + "-" + day)){
                        dateGet = "Today";
                    }
                    String dateTime = time+" / "+dateGet;

                    if(owner.equals(profileID)){
                        senderMessages.setText(message);
                        myMessages.setVisibility(View.GONE);
                        senderMessagesDateTime.setText(dateTime);
                        myMessagesDateTime.setVisibility(View.GONE);
                    }
                    else if(owner.equals(uID)){
                        myMessages.setText(message);
                        senderMessages.setVisibility(View.GONE);
                        myMessagesDateTime.setText(dateTime);
                        senderMessagesDateTime.setVisibility(View.GONE);
                    }
                    else{ //This is for general massages In this moment it is deactivated
                        myMessages.setVisibility(View.GONE);
                        myMessagesDateTime.setVisibility(View.GONE);
                        senderMessages.setVisibility(View.GONE);
                        senderMessagesDateTime.setVisibility(View.GONE);
                    }

                    chatsLinearLayout.addView(itemLayout);
                    gotoBottomBtn.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}