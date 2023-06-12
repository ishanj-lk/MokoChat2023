package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainInterface extends AppCompatActivity {

    private String uID;
    private FirebaseDatabase FBdatabase;
    private BottomNavigationView bottomNavigationView;

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

        //Fragment Management
        fragmentManager();
        //Check for new user
        newUserCheck();


        //This initialize UI elements
        elementInitialize();
        //This triggers main actions
        actionTriggers();
    }

    private void fragmentManager() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigationBar);

        chatsFragment chatsFragment = new chatsFragment();
        searchFragment searchFragment = new searchFragment();
        settingsFragment settingsFragment = new settingsFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.container, chatsFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuChats:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, chatsFragment).commit();
                        return true;
                    case R.id.menuSearch:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFragment).commit();
                        return true;
                    case R.id.menuSettings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
                        return true;
                }
                return false;
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

    }

    private void elementInitialize() {


    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}

