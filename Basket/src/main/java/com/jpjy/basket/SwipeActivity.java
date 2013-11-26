package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import com.jpjy.basket.MainActivity.EventHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SwipeActivity extends Activity {
    Intent intent;
    private static final int RFIDCARD = 0x0010;
    private RfidThread mRfidThread;
    private MyApplication myApplication;
    private EventHandler mEventHandler;

    private boolean isRun;
    private FileInputStream mRfidCard;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.swipe);

        try {
            mRfidCard = openFileInput("/dev/RFID");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (mRfidThread == null)
            mRfidThread = new RfidThread();
        mRfidThread.start();

        myApplication = (MyApplication) getApplication();
        mEventHandler = myApplication.getHandler();
    }

    protected void onResume() {
        super.onResume();

        if (mRfidThread == null) {
            mRfidThread = new RfidThread();
            mRfidThread.start();
            isRun = true;
        }
    }

    protected void onPause() {
        super.onPause();
        // Stop the Read card thread
        if (mRfidThread != null) {
            isRun = false;
            mRfidThread.interrupt();
            mRfidThread = null;
        }
    }

    private class RfidThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (isRun) {
                try {

                    byte[] BufferRfid = new byte[4];

                    if (BufferRfid != null)
                        mRfidCard.read(BufferRfid);

                    Message msg = mEventHandler.obtainMessage(RFIDCARD, BufferRfid.toString());
                    mEventHandler.sendMessage(msg);
                    //mBarcode.read(mBufferBarcode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
