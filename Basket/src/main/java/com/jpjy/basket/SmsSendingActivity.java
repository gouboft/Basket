package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SmsSendingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.smssending);

        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra("PhoneNumber");
        TextView tv = (TextView) findViewById(R.id.pn);
        tv.setText(phoneNumber);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}