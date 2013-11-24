package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class OpenActivity extends Activity {
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.open);

        new Handler().postDelayed(new Runnable() {

            public void run() {
                Intent intent = new Intent(OpenActivity.this, ChioceActivity.class);
                OpenActivity.this.startActivity(intent);
                OpenActivity.this.finish();
            }
        }, 20000);
    }

}
