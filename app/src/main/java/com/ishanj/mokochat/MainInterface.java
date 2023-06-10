package com.ishanj.mokochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.paperdb.Paper;

public class MainInterface extends AppCompatActivity {

    private String uID;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);

        Paper.init(MainInterface.this);

        uID = Paper.book().read("uID");

        Toast.makeText(this, uID, Toast.LENGTH_SHORT).show();

        //This initialize UI elements
        elementInitialize();
        //This triggers main actions
        actionTriggers();

    }

    private void actionTriggers() {
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logoutIntent = new Intent(MainInterface.this, MainActivity.class);
                startActivity(logoutIntent);
                Paper.book().delete("uID");
            }
        });
    }

    private void elementInitialize() {
        btnLogout = (Button) findViewById(R.id.logout);
    }
}