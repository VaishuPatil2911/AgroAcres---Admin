package com.example.foodappclient.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.foodappclient.ChoiceActivity;
import com.example.foodappclient.PreferenceManager;
import com.example.foodappclient.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {


    ActivityLoginBinding binding;
    String email = "admin@gmail.com";
    String password = "1234567";

    PreferenceManager preferenceManager;
    DatabaseReference reference;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager  = new PreferenceManager(this);
        reference = FirebaseDatabase.getInstance().getReference().child("Seller");
        if (preferenceManager.getBoolean("signed")){
            startActivity(new Intent(LoginActivity.this, ChoiceActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }

        auth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> {
            String em = binding.email.getText().toString();
            String pw = binding.password.getText().toString();

            if (em.isEmpty() || pw.isEmpty()){
                Toast.makeText(LoginActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(LoginActivity.this, "Enter valid email address", Toast.LENGTH_SHORT).show();

            }else {

                ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Logging...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                auth.signInWithEmailAndPassword(em,pw).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        startActivity(new Intent(LoginActivity.this, ChoiceActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                        preferenceManager.putBoolean("signed",true);
                        progressDialog.dismiss();
                    }else {
                        Toast.makeText(LoginActivity.this, "Failed: "+task.getException()
                                .getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

            }

        });

    }
}