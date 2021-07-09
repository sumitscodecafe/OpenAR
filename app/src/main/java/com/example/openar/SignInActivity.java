package com.example.openar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {

    private EditText editText;
    private TextView errorMsg, resendCode;
    private ProgressBar progressBar;
    private Button btn_getOTP, btn_signIn;
    private String phoneNumber, otp;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editText = findViewById(R.id.editText);
        btn_getOTP = findViewById(R.id.btn_getOTP);
        btn_signIn = findViewById(R.id.btn_signIn);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);
        errorMsg = findViewById(R.id.textErrorMsg);
        errorMsg.setVisibility(View.INVISIBLE);
        resendCode = findViewById(R.id.resendCode);
        resendCode.setVisibility(View.INVISIBLE);
        resendCode.setEnabled(false);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btn_signIn.setVisibility(View.INVISIBLE);
        btn_signIn.setEnabled(false);
        btn_getOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumber = editText.getText().toString();
                if(!phoneNumber.equals("")){
                    requestOtp(phoneNumber);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Enter phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                signIn(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                errorMsg.setText(R.string.error_msg);
                errorMsg.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Verification failed!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    Toast.makeText(getApplicationContext(), "Invalid request, try again", Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(getApplicationContext(), "Too many requests, please wait or try another number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                editText.setText("");
                editText.setHint("Enter OTP");
                Toast.makeText(getApplicationContext(), "OTP has been sent", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                errorMsg.setVisibility(View.INVISIBLE);
                //If OTP not detected automatically, manually enter OTP and sign-in:
                //hide get_otp button
                btn_getOTP.setVisibility(View.INVISIBLE);
                btn_getOTP.setEnabled(false);
                //show sign_in button
                btn_signIn.setVisibility(View.VISIBLE);
                btn_signIn.setEnabled(true);
                btn_signIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        otp = editText.getText().toString();
                        if(!otp.equals("")){
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                            signIn(credential);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Enter OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                resendCode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestOtp(phoneNumber);
                        resendCode.setVisibility(View.INVISIBLE);
                        resendCode.setEnabled(false);
                        progressBar.setVisibility(View.INVISIBLE);
                        String codeSentText = "OTP re-sent to: "+phoneNumber;
                        errorMsg.setText(codeSentText);
                        errorMsg.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                errorMsg.setVisibility(View.INVISIBLE);
                                resendCode.setText(R.string.resendMsg);
                                resendCode.setEnabled(true);
                                resendCode.setVisibility(View.VISIBLE);
                            }
                        }, 60000);
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resendCode.setText(R.string.resendMsg);
                        resendCode.setEnabled(true);
                        resendCode.setVisibility(View.VISIBLE);
                    }
                }, 10000);
            }
        };
    }

    private void requestOtp(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreference.saveSharedSetting(getApplicationContext(), "ph_no", phoneNumber);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(SignInActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Toast.makeText(getApplicationContext(), "OTP requested", Toast.LENGTH_SHORT).show();
    }

    public void signIn(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()) {
                    errorMsg.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Verification complete!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Code does not match! Try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and goto Main activity (ARcore)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }
    }


}