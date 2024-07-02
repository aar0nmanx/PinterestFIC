package com.app.pinterest.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.pinterest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    EditText edtEmail, edtPassword;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if(email.isEmpty()){
                    edtEmail.setError("Required!");
                    edtEmail.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    edtPassword.setError("Required!");
                    edtPassword.requestFocus();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                builder.setTitle("Choose");
                builder.setMessage("Do you want to Sign In or Create new Account?").setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signInOrCreateAccount(email,password);
                    }
                }).setNegativeButton("Create New", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createAccount(email, password);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }


    private void signInOrCreateAccount(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, account exists
                            Log.d("LoginActivity", "signInWithEmailPassword:success");
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        } else {
                            Toast.makeText(SignInActivity.this, "Invalid details!", Toast.LENGTH_SHORT).show();
                            Log.w("LoginActivity", "signInWithEmailPassword:failure", task.getException());
                        }
                    }
                });
    }
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Account creation success
                            Log.d("LoginActivity", "createUserWithEmailPassword:success");
                            signInOrCreateAccount(email,password);
                        } else {
                            // Account creation failed
                            Log.w("LoginActivity", "createUserWithEmailPassword:failure", task.getException());
                            // You can handle different cases here, like showing error messages to the user
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();



        if(isOnline()){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
    boolean isOnline(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            return true;
        }
        return false;
    }
}