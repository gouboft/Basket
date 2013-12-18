package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jpjy.basket.MainActivity.EventHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneNumConfirmActivity extends Activity {
    private static final String TAG = "PhoneNumConfirmActivity";
    private static final int PHONENUM = 0x0010;

    private Intent intent;
    private boolean isInput = false;
    private String mPhoneNumber;
    private int boxNum;
    private EventHandler handler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.phonenumconfirm);


        MyApplication myApplication = (MyApplication) getApplication();
        handler = myApplication.getHandler();

        Intent intent = getIntent();
        mPhoneNumber = intent.getStringExtra("PhoneNumber");
        boxNum = intent.getIntExtra("BoxNum", 0);
        TextView tv = (TextView) findViewById(R.id.phonenumber);
        tv.setText(FormatPhoneNum(mPhoneNumber));
    }
    private String FormatPhoneNum(String phoneNumber) {
        Pattern regex = Pattern.compile("^\\(?([0-9]{3})\\)?[-. ]?([0-9]{4})[-.?]?([0-9]{4}){1}quot;");
        String formattedNumber = "";
        Matcher regexMatcher = regex.matcher(phoneNumber);
        if(regexMatcher.find()){
            formattedNumber = regexMatcher.replaceAll("$1-$2-$3");
        }
        return formattedNumber;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                isInput = true;
                intent = new Intent(PhoneNumConfirmActivity.this,
                        PhoneNumInputActivity.class);
                startActivity(intent);
                PhoneNumConfirmActivity.this.finish();
                break;
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() != KeyEvent.ACTION_UP) {

            Message msg = handler.obtainMessage(PHONENUM, boxNum, 0, mPhoneNumber);
            handler.sendMessage(msg);

            intent = new Intent(PhoneNumConfirmActivity.this,
                    SmsSendingActivity.class);
            intent.putExtra("PhoneNumber", mPhoneNumber);
            startActivity(intent);
            Log.d(TAG, "PhoneNumber is " + mPhoneNumber);

            return true;
        }
        return super.dispatchKeyEvent(event);
    }


}
