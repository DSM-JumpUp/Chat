package com.chat.jumpup.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class ConnectActivity extends AppCompatActivity {

    private Button connectStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        connectStartButton = (Button) findViewById(R.id.btn_connect_start);
    }
}
