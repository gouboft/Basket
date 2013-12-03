package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.jpjy.basket.MainActivity.EventHandler;

public class RfidcardActivity extends Activity {
    private static final int RFIDCARD = 0x0010;
    private static final String TAG = "RfidcardActivity";
    private RfidThread mRfidThread;
    private EventHandler mEventHandler;


    private boolean isInput;
    private String mCardNumber;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.rfidcard);

        MyApplication myApplication = (MyApplication) getApplication();
        mEventHandler = myApplication.getHandler();

        int fd = Linuxc.openUart(Config.RFIDCardDevice);
        if(fd > 0) {
            Log.d(TAG, Config.RFIDCardDevice + " is open");
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(RfidcardActivity.this, ChoiceActivity.class);
                    RfidcardActivity.this.startActivity(intent);
                    Linuxc.closeUart();
                }
            }
        }, 20000);
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mRfidThread == null) {
            mRfidThread = new RfidThread();
            mRfidThread.start();
        }
    }

    protected void onPause() {
        super.onPause();
        if (mCardNumber != null)
            Toast.makeText(RfidcardActivity.this, "读到卡： " + mCardNumber, Toast.LENGTH_LONG).show();
        // Stop the Read card thread
        if (mRfidThread != null) {
            mRfidThread = null;
        }
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isInput = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                Intent intent = new Intent(RfidcardActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);
                RfidcardActivity.this.finish();
                break;
        }
        return true;
    }

    private class RfidThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                //TODO check the password, if the password is changed, write it to kernel

                mCardNumber = Linuxc.receiveMsgUart();
                if(mCardNumber != null) {
                    Log.d(TAG, "RFID card number is " + mCardNumber);
                    isInput = true;
                    Linuxc.closeUart();
                    mRfidThread.interrupt();
                    Message msg = mEventHandler.obtainMessage(RFIDCARD, 0, 0, mCardNumber);
                    mEventHandler.sendMessage(msg);
                }
            }
        }
    }
}