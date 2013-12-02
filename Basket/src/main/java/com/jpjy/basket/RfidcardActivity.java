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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RfidcardActivity extends Activity {
    private static final int RFIDCARD = 0x0010;
    private static final String TAG = "RfidcardActivity";
    private RfidThread mRfidThread;
    private EventHandler mEventHandler;

    private FileInputStream mRfidCard;

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

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(RfidcardActivity.this, ChoiceActivity.class);
                    RfidcardActivity.this.startActivity(intent);
                    RfidcardActivity.this.finish();
                }
            }
        }, 20000);
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        try {
            mRfidCard = new FileInputStream("/dev/rfid_rc522_dev");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (mRfidThread == null) {
            mRfidThread = new RfidThread();
            mRfidThread.start();
        }
    }

    protected void onPause() {
        super.onPause();
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
                try {
                    byte[] BufferRfid = new byte[4];

                    if (mRfidCard != null) {
                        int num = mRfidCard.read(BufferRfid);
                            mCardNumber = Integer.toHexString(byte2int(BufferRfid)).toUpperCase();
                        if (num != 4)
                            return;
                        Log.d(TAG, "RFID card number is " + mCardNumber);
                        isInput = true;
                        mRfidCard.close();
                        mRfidThread.interrupt();
                        Message msg = mEventHandler.obtainMessage(RFIDCARD, 0, 0, mCardNumber);
                        mEventHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int byte2int(byte[] res) {
        return (res[3] & 0xff) | ((res[2] << 8) & 0xff00)
                | ((res[1] << 24) >>> 8) | (res[0] << 24);
    }
}
