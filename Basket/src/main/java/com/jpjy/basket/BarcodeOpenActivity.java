package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BarcodeOpenActivity extends Activity {
    private int boxNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barcodeopen);

        Intent intent = getIntent();
        boxNum = intent.getIntExtra("BoxNum", 0);
        TextView tv = (TextView) findViewById(R.id.in1);
        tv.setText(String.valueOf(boxNum));
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && event.getAction() != KeyEvent.ACTION_UP) {

            Intent intent = new Intent(BarcodeOpenActivity.this, PhoneNumInputActivity.class);
            intent.putExtra("BoxNum", boxNum);
            startActivity(intent);
            return true;
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DEL
                && event.getAction() != KeyEvent.ACTION_UP) {
            Intent intent = new Intent(BarcodeOpenActivity.this, ChoiceActivity.class);
            startActivity(intent);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}
