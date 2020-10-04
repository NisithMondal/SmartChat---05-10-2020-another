package com.nisith.smartchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        TextView textView = findViewById(R.id.text_view);
        Intent intent = getIntent();
        String senderUid = intent.getStringExtra("senderUid");
        if (senderUid != null){
            textView.setText(senderUid);
        }
    }
}