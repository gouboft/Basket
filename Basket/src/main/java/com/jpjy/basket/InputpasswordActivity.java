package com.jpjy.basket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jpjy.basket.MainActivity.EventHandler;

public class InputpasswordActivity extends Activity {
    private static final int PASSWORD = 0x0001;
    EditText pass;
    Intent intent;
    private MyApplication myApplication;
    private int password;
    private EventHandler handler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.inputpassword);

        myApplication = (MyApplication) getApplication();
        handler = myApplication.getHandler();

        pass = (EditText) findViewById(R.id.password);
        pass.requestFocus();
        pass.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                intent = new Intent(InputpasswordActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);
                InputpasswordActivity.this.finish();
                break;
        }
        return true;
    }


    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            String password = pass.getText().toString();

            if (password.length() < 6) {
                Toast.makeText(InputpasswordActivity.this, "输入的密码小于6位", Toast.LENGTH_LONG)
                        .show();
                intent = new Intent(InputpasswordActivity.this, PasswordFailActivity.class);
                intent.putExtra("ErrorReason", "输入的密码小于6位");
                startActivity(intent);
            } else if (password.length() == 6) {
                Message msg = handler.obtainMessage(PASSWORD, Integer.parseInt(password));
                handler.sendMessage(msg);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
