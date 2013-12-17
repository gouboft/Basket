package com.jpjy.basket;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.apache.http.util.EncodingUtils;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    private static final boolean Debug = Config.Debug;
    private static final String TAG = "MainActivity";

    private static final int PASSWORD = 0x0001;
    private static final int PHONENUM = 0x0010;
    private static final int DOWNLOAD = 0x0100;
    private static final int UPLOAD = 0x0101;
    private static final int BARCODE = 0x1000;

    private final int NETWORK_TYPE = ConnectivityManager.TYPE_MOBILE;

    private DomService domService;

    private Context mContext;

    private Thread mDownloadThread;
    private Thread mUploadThread;
    private boolean waitNetworkToDownload;
    private boolean waitNetworkToUpload;
    private ConnectivityManager mConnectivityManager;
    private NetworkStatusReceiver mReceiver;

    private EventHandler mEventHandler;
    private List<Data> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        MyApplication myApp = (MyApplication) getApplication();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);


        HandlerThread ht = new HandlerThread("EventHandler");
        ht.start();
        mEventHandler = new EventHandler(ht.getLooper());
        myApp.setHandler(mEventHandler);

        domService = new DomService();
        mData = new ArrayList<Data>();

        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mReceiver = new NetworkStatusReceiver();
        IntentFilter mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mReceiver, mIntentFilter);

        try {
            String ret = readFile("data.xml");
            if (!ret.equals(""))
                mData = domService.getDataResult(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, ChoiceActivity.class);
                MainActivity.this.startActivity(intent);
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Debug) Log.d(TAG, "onDestroy()");
        mContext.unregisterReceiver(mReceiver);
        mDownloadThread.interrupt();
        mDownloadThread = null;
        mUploadThread.interrupt();
        mUploadThread = null;
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private boolean checkNetworkInfo() {
        NetworkInfo ni = mConnectivityManager.getNetworkInfo(NETWORK_TYPE);
        if (ni != null) {
            State state = ni.getState();
            return state == State.CONNECTED;
        } else
            return false;
    }

    public void writeFile(String fileName, String writeStr) throws IOException {
        try {
            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = writeStr.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception ignored) {
        }
    }

    public String readFile(String fileName) throws IOException {
        String res = "";
        try {
            FileInputStream fin = openFileInput(fileName);

            int length = fin.available();
            byte[] buffer = new byte[length];
            if (fin.read(buffer) > 0) {
                res = EncodingUtils.getString(buffer, "UTF-8");
                fin.close();
            }
        } catch (Exception ignored) {
        }
        return res;
    }

    private class NetworkStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo info = mConnectivityManager.getNetworkInfo(
                        NETWORK_TYPE);
                if (info == null)
                    return;
                if (Debug) Log.d(TAG, "NetWork Type is " + info.getType() +
                        " and it's state is " + info.getState());

                if (info.getType() == NETWORK_TYPE &&
                        info.getState() == State.CONNECTED) {
                    if (waitNetworkToDownload) {
                        Message msg = mEventHandler.obtainMessage(DOWNLOAD);
                        mEventHandler.sendMessage(msg);

                    } else if (waitNetworkToUpload) {
                        Message msg = mEventHandler.obtainMessage(UPLOAD);
                        mEventHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    private boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    final class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void handleMessage(Message msg) {
            if (Debug) Log.d(TAG, "Handle Message");
            super.handleMessage(msg);
            synchronized (this) {
                if (msg.what == PASSWORD) {
                    if (Debug) Log.d(TAG, "Handle Password");
                    String password = (String) msg.obj;
                    int tag = msg.arg1;
                    //if input is not all number, we display error
                    if (!isNumeric(password)) {
                        Intent intent = new Intent(MainActivity.this, PasswordFailActivity.class);
                        intent.putExtra("ErrorReason", "密码错误");
                        startActivity(intent);
                        return;
                    }

                    int boxNum = checkPassword(Integer.parseInt(password));
                    if (boxNum > 0) {
                        Intent intent = new Intent(MainActivity.this, PasswordOpenActivity.class);
                        intent.putExtra("BoxNum", boxNum);
                        startActivity(intent);
                    } else if (boxNum == 0) {
                        Intent intent = new Intent(MainActivity.this, PasswordFailActivity.class);
                        intent.putExtra("ErrorReason", "密码错误");
                        startActivity(intent);
                    }
                } else if (msg.what == BARCODE) {
                    if (Debug) Log.d(TAG, "Handle BarCode");
                    String barCode = (String) msg.obj;
                    int tag = msg.arg1;
                    // TODO choose a EMPTY cabinet to open
                    int boxNum = openEmptyCabinet(barCode);
                    if (boxNum == 0) {
                        //TODO No cabinet is empty, display fail UI

                    } else {
                        Intent intent = new Intent(MainActivity.this, BarcodeOpenActivity.class);
                        intent.putExtra("BoxNum", boxNum);
                        startActivity(intent);
                    }
                } else if (msg.what == PHONENUM) {
                    String phoneNumber = (String) msg.obj;
                    int boxNumber = msg.arg1;
                    int password = generatePassword();
                    sendShortMessage(boxNumber, password, phoneNumber);
                }
            }
        }
    }

    private int openEmptyCabinet(String barCode) {
        return 0;
    }

    private int generatePassword() {
        return 123456;
    }

    private void sendShortMessage(int boxNumber, int password, String phoneNumber) {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        String message = generateSmsContent(boxNumber, password);
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    private String generateSmsContent(int boxNumber, int password) {
        return "你的包裹已经发在" + boxNumber + "号箱，开箱密码为：" + password + "，请尽快取出，谢谢。";
    }


    private int checkPassword(int password) {
        int boxNum;
        for (Data data : mData) {
            if (password == data.getPassword()) {
                boxNum = data.getBoxNo();

                openDoor(boxNum);

                mData.remove(data);
                try {
                    writeFile("upload.xml", domService.putUpload(mUpload));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return boxNum;
            }
        }
        return 0;
    }

    private void openDoor(int doorNo) {
        int fd;

        fd = Linuxc.openUart(Config.electronicLockDevice);
        if (fd < 0) {
            Log.e(TAG, "Hardware error");
            return;
        }
        Linuxc.setUart(Config.electronicLockBaudRate);

        Linuxc.sendHexUart(keyOfLock(doorNo));
        Linuxc.closeUart();

        if (Debug) Log.d(TAG, "The number of the door  " + doorNo + " is open!");
    }

    private int[] keyOfLock(int doorNo) {
        int[] key = {0x8A, 0x01, doorNo, 0x11, 0x9B};
        if (key[2] / 8 != 0)
            key[1] = key[2] / 8 + 1;

        key[4] = key[0] ^ key[1] ^ key[2] ^ key[3];
        return key;
    }

    private class Data {
        private String phoneNumber;
        private int password;
        private int boxNumber;
        private String recordTime;
    }

}
