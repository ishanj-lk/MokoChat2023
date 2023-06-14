package com.ishanj.mokochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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
    private StorageReference mStorageRef;
    private ImageView profilePicture;

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
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
//        uploadPicture();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            mStorageRef = FirebaseStorage.getInstance().getReference();
            Uri imageUri = data.getData();
            StorageReference imageRef = mStorageRef.child("images/"+uID+".jpg");
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Listen for the upload to complete
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                       @Override
                       public void onSuccess(Uri downloadUri) {
                           // Handle the retrieved download URL
                           String imageUrl = downloadUri.toString();
                           updateImage(imageUrl);
                       }

                   });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Display a toast message
                    Toast.makeText(EditUserInfo.this, "Image upload failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateImage(String imageUrl) {
        DatabaseReference imageRef = FBdatabase.getReference("users").child(uID);
        Map<String, Object> registerUserData = new HashMap<>();
        registerUserData.put("imageUrl", imageUrl);
        imageRef.updateChildren(registerUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(EditUserInfo.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
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

                    try {
                        String imageUrl = snapshot.child("imageUrl").getValue().toString();
                        Picasso picasso = Picasso.get();
                        picasso.load(imageUrl).resize(200, 200).
                                transform(new RoundedTransformation(10, 10)).centerCrop().into(profilePicture);
                    } catch (Exception e) {
                        // Handle the exception.
                    }

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
        registerUserRef.updateChildren(registerUserData).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        profilePicture = (ImageView) findViewById(R.id.profilePicture);
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}