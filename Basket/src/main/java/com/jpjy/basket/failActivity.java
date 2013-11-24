package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class failActivity extends Activity {

    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.fail);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                intent = new Intent(failActivity.this,
                        InputpasswordActivity.class);
                startActivity(intent);
                failActivity.this.finish();
                break;
        }
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            intent = new Intent(failActivity.this,
                    InputpasswordActivity.class);
            startActivity(intent);

            return true;

        }
        //failActivity.this.finish();
        return super.dispatchKeyEvent(event);
    }


}
