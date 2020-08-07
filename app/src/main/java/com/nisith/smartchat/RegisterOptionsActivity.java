package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class RegisterOptionsActivity extends AppCompatActivity {

    private Toolbar appToolbar;
    private TextView toolbarTextView;
    private ProgressBar progressBar;
    private Button createAccountWithEmailButton, googleSignInButton;
    private TextView haveAnAccountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_options);
        initializeViews();
        setSupportActionBar(appToolbar);
        setTitle("");
        toolbarTextView.setText("Sign Up Options");
        appToolbar.setNavigationIcon(R.drawable.ic_back_arrow_icon);
        appToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        createAccountWithEmailButton.setOnClickListener(new MyClickListener());
        googleSignInButton.setOnClickListener(new MyClickListener());
        haveAnAccountTextView.setOnClickListener(new MyClickListener());
        progressBar.setVisibility(View.GONE);
    }


    private void initializeViews(){
        appToolbar = findViewById(R.id.app_toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        progressBar = findViewById(R.id.progress_bar);
        createAccountWithEmailButton = findViewById(R.id.create_account_with_email_button);
        googleSignInButton = findViewById(R.id.google_signIn_button);
        haveAnAccountTextView = findViewById(R.id.already_have_account_text_view);

    }


    private class MyClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.create_account_with_email_button:
                    startActivity(new Intent(RegisterOptionsActivity.this, RegisterActivity.class));
                    break;

                case R.id.google_signIn_button:
                    Toast.makeText(RegisterOptionsActivity.this, "google_signIn_button", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.already_have_account_text_view:
                    startActivity(new Intent(RegisterOptionsActivity.this, LoginActivity.class));
                    break;
            }

        }
    }

}