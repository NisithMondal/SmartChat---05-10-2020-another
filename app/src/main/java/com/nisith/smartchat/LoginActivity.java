package com.nisith.smartchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.collection.LLRBNode;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private ProgressBar progressBar;
    private EditText emailEditText, passwordEditText;
    private TextView forgotPasswordTextView, signUpTextView;
    private Button loginButton;
    //Firebase
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeViews();
        setSupportActionBar(appToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("User Login");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        forgotPasswordTextView.setOnClickListener(new MyClickListener());
        signUpTextView.setOnClickListener(new MyClickListener());
        loginButton.setOnClickListener(new MyClickListener());
        progressBar.setVisibility(View.GONE);
        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
    }



    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        progressBar = findViewById(R.id.progress_bar);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        forgotPasswordTextView = findViewById(R.id.forgot_password_text_view);
        signUpTextView = findViewById(R.id.sing_up_text_view);
        loginButton = findViewById(R.id.login_button);
    }

    private class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.forgot_password_text_view:
                    forgotPassword(view);
                    break;

                case R.id.login_button:
                    userLogin();
                    break;

                case R.id.sing_up_text_view:
                    startActivity(new Intent(LoginActivity.this, RegisterOptionsActivity.class));
                    break;
            }

        }
    }

    private void userLogin(){
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)){
            emailEditText.setError("Enter Email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)){
            passwordEditText.setError("Enter Password");
            passwordEditText.requestFocus();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        if (task.isSuccessful()){
                            openHomeActivity();
                            finishAffinity();
                        }else {
                            Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void forgotPassword(View view){
        final EditText editText = new EditText(view.getContext());
        editText.setHint("Enter Email Address");
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext())
                .setView(editText)
                .setPositiveButton("Change Password", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = editText.getText().toString();
                        if (TextUtils.isEmpty(email)){
                            Toast.makeText(LoginActivity.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        firebaseAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()){
                                            Toast.makeText(LoginActivity.this, "New password is sent to your email address. Please Check out on your Email.", Toast.LENGTH_LONG).show();
                                        }else {
                                            Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                })
                .setNegativeButton("Calcel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setTitle("Change Password");
        dialogBuilder.show();

    }

    private void openHomeActivity(){
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
    }

}