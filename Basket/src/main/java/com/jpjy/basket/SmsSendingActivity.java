package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsSendingActivity extends Activity {
    private static final String TAG = "SmsSendingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.smssending);

        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra("PhoneNumber");
        String prompt = intent.getStringExtra("Prompt");

        TextView tv = (TextView) findViewById(R.id.pn);
        tv.setText(FormatPhoneNum(phoneNumber));
        if (prompt != null && prompt.equals("已发送到:")) {
            TextView tv1 = (TextView) findViewById(R.id.prompt);
            tv1.setText(prompt);
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(SmsSendingActivity.this, ChoiceActivity.class);
                SmsSendingActivity.this.startActivity(intent);
            }
        }, 5000);
    }


    private String FormatPhoneNum(String phoneNumber) {
        Pattern regex = Pattern.compile("([0-9]{3})([0-9]{4})([0-9]{4})");
        String formattedNumber = "";
        Matcher regexMatcher = regex.matcher(phoneNumber);
        if(regexMatcher.find()){
            formattedNumber = regexMatcher.replaceAll("$1-$2-$3");
        }
        return formattedNumber;
    }
}