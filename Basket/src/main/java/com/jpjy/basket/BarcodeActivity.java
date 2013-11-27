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
import com.jpjy.basket.MainActivity.EventHandler;

public class BarcodeActivity extends Activity {
    private static final String TAG = "BarcodeActivity";
    private static final int BARCODE = 0x1000;

    private Intent intent;

    private BarcodeThread mBarcodeThread;
    private MyApplication myApplication;
    private EventHandler mEventHandler;

    private int fd;
    private String mBarcode;
    private boolean isInput;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barcode);

        if (mBarcodeThread == null)
            mBarcodeThread = new BarcodeThread();
        mBarcodeThread.start();

        fd = Linuxc.openUart("/dev/ttyS1");
        if(fd > 0) {
            Linuxc.setUart(9600);
            Log.d(TAG, "ttyS6 is open");
        }

        myApplication = (MyApplication) getApplication();
        mEventHandler = myApplication.getHandler();

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(BarcodeActivity.this, ChoiceActivity.class);
                    BarcodeActivity.this.startActivity(intent);
                    BarcodeActivity.this.finish();
                }
            }
        }, 30000);
    }

    protected void onResume() {
        super.onResume();

        if (mBarcodeThread == null) {
            mBarcodeThread = new BarcodeThread();
            mBarcodeThread.start();
        }
    }

    protected void onPause() {
        super.onPause();
        // Stop the Read card thread
        Linuxc.closeUart();
        if (mBarcodeThread != null) {
            mBarcodeThread.interrupt();
            mBarcodeThread = null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isInput = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                intent = new Intent(BarcodeActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);
                BarcodeActivity.this.finish();
                break;
        }
        return true;
    }

    private class BarcodeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                mBarcode = "";
                mBarcode = Linuxc.receiveMsgUart();
                isInput = true;
                Log.d(TAG, "Barcode = " + mBarcode);
                if (mBarcode != null) {
                    Message msg = mEventHandler.obtainMessage(BARCODE, mBarcode);
                    mEventHandler.sendMessage(msg);
                }
            }
        }
    }
}