package com.ishanj.mokochat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditUserInfo extends AppCompatActivity {

    private EditText editUserInfoNameGet, editUserInfoTownGet;
    private Button editUserInfoUpdateBtnGet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        //This initialize UI elements
        elementInitialize();
        //This triggers main actions
        actionTriggers();
    }

    private void actionTriggers() {
        editUserInfoUpdateBtnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(String.valueOf(editUserInfoUpdateBtnGet)) && !TextUtils.isEmpty(String.valueOf(editUserInfoTownGet))){
                    updateUserInfo();
                }
                else{
                    Toast.makeText(EditUserInfo.this, "Please fill all fields...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserInfo() {
    }

    private void elementInitialize() {
        editUserInfoNameGet = (EditText) findViewById(R.id.editUserInfoName);
        editUserInfoTownGet = (EditText) findViewById(R.id.editUserInfoTown);

        editUserInfoUpdateBtnGet = (Button) findViewById(R.id.editUserInfoUpdateBtn);
    }
}