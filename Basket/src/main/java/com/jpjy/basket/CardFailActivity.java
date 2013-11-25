package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CardFailActivity extends Activity {

    private Intent intent;
    private boolean isInput = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.failreason);

        Intent intent = getIntent();
        String er = intent.getStringExtra("ErrorReason");

        TextView tv = (TextView) findViewById(R.id.ou);
        tv.setText(er);

        // Auto back to ChoiseActivity if no input event in 20s
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(CardFailActivity.this, ChoiceActivity.class);
                    CardFailActivity.this.startActivity(intent);
                    CardFailActivity.this.finish();
                }
            }
        }, 20000);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                isInput = true;
                intent = new Intent(CardFailActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);
                CardFailActivity.this.finish();
                break;
        }
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            isInput = true;
            intent = new Intent(CardFailActivity.this,
                    SwipeActivity.class);
            startActivity(intent);

            return true;

        }
        //CardFailActivity.this.finish();
        return super.dispatchKeyEvent(event);
    }


}
