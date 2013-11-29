package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.jpjy.basket.MainActivity.EventHandler;

public class PasswordActivity extends Activity {
    private static final String TAG = "PasswordActivity";
    private static final int PASSWORD = 0x0001;
    private EditText pass;
    private Intent intent;
    private MyApplication myApplication;

    private EventHandler handler;
    private boolean isInput = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.password);

        myApplication = (MyApplication) getApplication();
        handler = myApplication.getHandler();

        pass = (EditText) findViewById(R.id.password);
        pass.requestFocus();
        pass.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(PasswordActivity.this, ChoiceActivity.class);
                    PasswordActivity.this.startActivity(intent);
                    PasswordActivity.this.finish();
                }
            }
        }, 30000);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isInput = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                intent = new Intent(PasswordActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);
                PasswordActivity.this.finish();
                break;
        }
        return true;
    }


    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() != KeyEvent.ACTION_UP) {
            String password = pass.getText().toString();
            isInput = true;
            if (password.length() < 6) {
                intent = new Intent(PasswordActivity.this, PasswordFailActivity.class);
                intent.putExtra("ErrorReason", "输入的密码小于6位");
                startActivity(intent);
            } else if (password.length() == 6) {
                Log.d(TAG, "Password is 6 digit" + password);
                Message msg = handler.obtainMessage(PASSWORD, 0, 0, password);
                handler.sendMessage(msg);
            }

        }
        return super.dispatchKeyEvent(event);
    }

}
