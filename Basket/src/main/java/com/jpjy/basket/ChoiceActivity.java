package com.jpjy.basket;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class ChoiceActivity extends Activity {

    private Intent intent;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }

    public void onPause() {
        super.onPause();
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                intent = new Intent(ChoiceActivity.this,
                        PasswordActivity.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_2:
                intent = new Intent(ChoiceActivity.this,
                        RfidcardActivity.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_3:
                intent = new Intent(ChoiceActivity.this,
                        BarcodeActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }


}
