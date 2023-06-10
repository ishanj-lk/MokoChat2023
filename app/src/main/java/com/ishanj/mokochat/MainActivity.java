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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

public class MainActivity<callbacks> extends AppCompatActivity {

    private EditText phoneNumber, otpValueGet;
    private Button phoneNumberSubmitBtn;
    private String phoneNumberTxt;
    private Boolean clickedOnce=Boolean.FALSE;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(MainActivity.this);
        mAuth = FirebaseAuth.getInstance();
        Paper.init(MainActivity.this);

        //This initialize UI elements
        elementInitialize();

        //This checks user already login, the redirect to main interface
        checkAlreadyLogin();
        //This triggers main actions
        actionTriggers();
    }

    private void actionTriggers() {
        phoneNumberSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(phoneNumber.getText().toString())){
                    Toast.makeText(MainActivity.this, "Please enter your phone number first...", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendPhoneNumber();
                }
            }
        });
    }

    private void elementInitialize() {
        phoneNumber = (EditText) findViewById(R.id.phoneNumberValue);
        otpValueGet = (EditText) findViewById(R.id.otpValue);
        phoneNumberSubmitBtn = (Button) findViewById(R.id.phoneNumberSubmit);
        otpValueGet.setVisibility(View.GONE);
    }

    private void sendPhoneNumber() {
        phoneNumberTxt = String.valueOf(phoneNumber.getText());
        sendOtp();
    }

    private void checkAlreadyLogin() {
        String uID = Paper.book().read("uID");
        if(uID !=null){
            Intent mainUIIntent = new Intent(MainActivity.this, MainInterface.class);
            startActivity(mainUIIntent);
        }
    }

    private void sendOtp() {
        if(clickedOnce == Boolean.FALSE){
            clickedOnce = Boolean.TRUE;
            phoneNumberSubmitBtn.setText("Send OTP");
            phoneNumber.setEnabled(false);
            otpValueGet.setVisibility(View.VISIBLE);
            startPhoneNumberVerification(String.valueOf(phoneNumberTxt));
        }
        else {
            String otpValueGetTxt = otpValueGet.getText().toString();
            if(TextUtils.isEmpty(otpValueGetTxt)) {
                Toast.makeText(MainActivity.this, "Please enter OTP number first...", Toast.LENGTH_SHORT).show();
            }
            else{
                verifyCode(otpValueGetTxt);
            }
        }

    }
    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
    mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        private FirebaseException e;

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            final String code = credential.getSmsCode();
            if(code!=null){
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            loginFailed();
        }

        @Override
        public void onCodeSent(@NonNull String s,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            verificationId = s;
        }
    };

    private void loginFailed() {
        Toast.makeText(MainActivity.this, "Login failed........", Toast.LENGTH_SHORT).show();
        Intent logoutIntent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(logoutIntent);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInByCredentials(credential);
    }
    private void signInByCredentials(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Login successful........", Toast.LENGTH_SHORT).show();
                    String uid = mAuth.getCurrentUser().getUid();
                    Paper.book().write("uID", uid);
                    Intent mainUIIntent = new Intent(MainActivity.this, MainInterface.class);
                    startActivity(mainUIIntent);
                }
                else {
                    loginFailed();
                }
                }
        });
    }


}