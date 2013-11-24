package com.jpjy.basket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import com.jpjy.basket.MainActivity.EventHandler;

import java.io.IOException;

public class SwipeActivity extends Activity {
    Intent intent;
    private static final int RFIDCARD = 0x0010;
    private MyApplication myApplication;
    private EventHandler mEventHandler;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.swipe);

        myApplication = (MyApplication) getApplication();
        mEventHandler = myApplication.getHandler();
    }


    private class CardBarcodeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {

                    byte[] BufferRfid = new byte[4];
                    //Todo: add barcode and then use poll or select

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
