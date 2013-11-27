package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class RfidcardFailActivity extends Activity {
    private Intent intent;
    private boolean isInput = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.rfidcardfail);

        Intent intent = getIntent();
        String er = intent.getStringExtra("ErrorReason");

        TextView tv = (TextView) findViewById(R.id.ou);
        tv.setText(er);

        // Auto back to ChoiseActivity if no input event in 20s
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(RfidcardFailActivity.this, ChoiceActivity.class);
                    RfidcardFailActivity.this.startActivity(intent);
                    RfidcardFailActivity.this.finish();
                }
            }
        }, 20000);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                isInput = true;
                intent = new Intent(RfidcardFailActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);
                RfidcardFailActivity.this.finish();
                break;
        }
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() != KeyEvent.ACTION_UP) {
            isInput = true;
            intent = new Intent(RfidcardFailActivity.this,
                    RfidcardActivity.class);
            startActivity(intent);

            return true;

        }
        //CardFailActivity.this.finish();
        return super.dispatchKeyEvent(event);
    }


}
