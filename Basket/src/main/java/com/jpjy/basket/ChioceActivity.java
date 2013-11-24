package com.jpjy.basket;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChioceActivity extends Activity {

    Intent intent;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_1:

                intent = new Intent(ChioceActivity.this,
                        InputpasswordActivity.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_2:
                intent = new Intent(ChioceActivity.this,
                        SwipeActivity.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_3:
                intent = new Intent(ChioceActivity.this,
                        BarActivity.class);
                startActivity(intent);
                break;

        }

        return true;
    }


}
