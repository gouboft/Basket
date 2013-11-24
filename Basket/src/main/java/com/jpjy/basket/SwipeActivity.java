package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.Window;
import android.view.WindowManager;

public class SwipeActivity extends Activity {
    Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.swipe);
    }


}
