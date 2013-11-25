package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class OpActivity extends Activity {
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ope);

        new Handler().postDelayed(new Runnable() {

            public void run() {
                Intent intent = new Intent(OpActivity.this, ChoiceActivity.class);
                OpActivity.this.startActivity(intent);
                OpActivity.this.finish();
            }
        }, 20000);
    }

}
