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

public class BarcodeActivity extends Activity {
    private static final String TAG = "BarcodeActivity";

    private final String ENTER_SETTING = "$$$$";
    private final String EXIT_SETTING = "%%%%";
    private final String ENTER_SETTING_SUCCESS = "@@@@";
    private final String EXIT_SETTING_SUCCESS  = "^^^^";

    private final String CommandStart   = "99900035";
    private final String CommandStop    = "99900036";

    private static final int BARCODE = 0x1000;

    private BarcodeThread mBarcodeThread;
    private EventHandler mEventHandler;

    private String mBarcode;
    private boolean isInput;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barcode);

        int fd = Linuxc.openUart(Config.barcodeDevice);
        if(fd > 0) {
            Linuxc.setUart(Config.barcodeBaudRate);
            Log.d(TAG, Config.barcodeDevice + " is open");
        }

        if (!initSetting() || !SettingValue(CommandStart))
            Log.d(TAG, "Hardware Error");

        MyApplication myApplication = (MyApplication) getApplication();
        mEventHandler = myApplication.getHandler();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (!isInput) {
                    Intent intent = new Intent(BarcodeActivity.this, ChoiceActivity.class);
                    BarcodeActivity.this.startActivity(intent);
                }
            }
        }, 20000);
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
        if (mBarcode != null)
            Toast.makeText(BarcodeActivity.this, "读到条码： " + mBarcode, Toast.LENGTH_LONG).show();
        // Stop the Read card thread
        if (mBarcodeThread != null) {
            mBarcodeThread.interrupt();
            mBarcodeThread = null;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SettingValue(CommandStop);
        Linuxc.closeUart();
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        isInput = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                Intent intent = new Intent(BarcodeActivity.this,
                        ChoiceActivity.class);
                startActivity(intent);

                break;
        }
        return true;
    }

    private class BarcodeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                mBarcode = Linuxc.receiveMsgUart();
                if (mBarcode != null) {
                    isInput = true;

                    Log.d(TAG, "Barcode = " + mBarcode);
                    mBarcodeThread.interrupt();
                    Message msg = mEventHandler.obtainMessage(BARCODE, 0, 0, mBarcode);
                    mEventHandler.sendMessage(msg);
                }
            }
        }
    }



    private boolean SettingValue(String setValue) {
        String receiveValue;

        //Enter setting
        Linuxc.sendMsgUart(ENTER_SETTING);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveValue = Linuxc.receiveMsgUart();
        if (receiveValue == null || !receiveValue.equals(ENTER_SETTING_SUCCESS))
            return false;

        //Send the setting value
        String sendValue = "#" + setValue + ";";
        Linuxc.sendMsgUart(sendValue);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveValue = Linuxc.receiveMsgUart();
        if (receiveValue == null || !receiveValue.equals("!" + setValue + ";"))
            return false;


        //Exit setting
        Linuxc.sendMsgUart(EXIT_SETTING);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveValue = Linuxc.receiveMsgUart();
        return !(receiveValue == null || !receiveValue.equals(EXIT_SETTING_SUCCESS));

    }

    private boolean initSetting() {
        String receiveValue;

        //Enter setting
        Linuxc.sendMsgUart(ENTER_SETTING);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveValue = Linuxc.receiveMsgUart();
        if (receiveValue == null || !receiveValue.equals(ENTER_SETTING_SUCCESS))
            return false;

        //Send the setting values
        String[] setValues = {"99900116", "99900142", "99910000", "99910002", "99900102"};
        for (String setValue : setValues) {
            String sendValue = "#" + setValue + ";";
            Linuxc.sendMsgUart(sendValue);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveValue = Linuxc.receiveMsgUart();
            if (receiveValue == null || !receiveValue.equals("!" + setValue + ";"))
                return false;
        }

        //Exit setting
        Linuxc.sendMsgUart(EXIT_SETTING);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        receiveValue = Linuxc.receiveMsgUart();

        return !(receiveValue == null || !receiveValue.equals(EXIT_SETTING_SUCCESS));

    }
}