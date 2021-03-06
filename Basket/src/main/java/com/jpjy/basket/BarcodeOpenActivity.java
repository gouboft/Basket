package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class BarcodeOpenActivity extends Activity {
    private Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.rfidcardopen);

        Intent intent = getIntent();
        int boxNum = intent.getIntExtra("BoxNum", 0);
        TextView tv = (TextView) findViewById(R.id.in1);
        tv.setText(String.valueOf(boxNum));

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(BarcodeOpenActivity.this, ChoiceActivity.class);
                BarcodeOpenActivity.this.startActivity(intent);
                BarcodeOpenActivity.this.finish();
            }
        }, 5000);
    }

}
