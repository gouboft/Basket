package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;


public class PhoneNumInputActivity extends Activity {
    private static final String TAG = "PhoneNumInputActivity";

    private EditText editText;
    private int boxNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.inputphonenumber);

        Intent intent = getIntent();
        boxNum = intent.getIntExtra("BoxNum", 0);

        editText = (EditText) findViewById(R.id.phonenumber);
        editText.requestFocus();
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() != KeyEvent.ACTION_UP) {
            Editable editable = editText.getText();
            String phoneNumber = "";
            if (editable != null)
                phoneNumber = editable.toString();

            if (phoneNumber.length() < 11) {
                Intent intent = new Intent(PhoneNumInputActivity.this, PhoneNumFailActivity.class);
                intent.putExtra("ErrorReason", "输入的手机号小于11位");
                startActivity(intent);
            } else if (phoneNumber.length() == 11) {
                Intent intent = new Intent(PhoneNumInputActivity.this, PhoneNumConfirmActivity.class);
                intent.putExtra("PhoneNumber", phoneNumber);
                intent.putExtra("BoxNum", boxNum);
                startActivity(intent);

            }
        }


        return super.dispatchKeyEvent(event);
    }
}