package com.jpjy.basket;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private static final boolean Debug = Config.Debug;
    private static final String TAG = "MainActivity";

    private static final int PASSWORD = 0x0001;
    private static final int PHONENUM = 0x0010;
    private static final int BARCODE = 0x1000;

    private static String mBarcode = "";

    private DomService mDomService;

    private List<Data> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        MyApplication myApp = (MyApplication) getApplication();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);


        HandlerThread ht = new HandlerThread("EventHandler");
        ht.start();
        EventHandler mEventHandler = new EventHandler(ht.getLooper());
        myApp.setHandler(mEventHandler);

        mDomService = new DomService();
        mData = new ArrayList<Data>();

        try {
            String result = readFile("data.xml");
            if (!result.equals(""))
                mData = mDomService.getData(result);
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
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
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

    private void sendMsg(String number, String message) {
        String SENT = "sms_sent";
        String DELIVERED = "sms_delivered";

        PendingIntent sentPI = PendingIntent.getActivity(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getActivity(this, 0, new Intent(DELIVERED), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("====>", "Activity.RESULT_OK");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.i("====>", "RESULT_ERROR_GENERIC_FAILURE");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.i("====>", "RESULT_ERROR_NO_SERVICE");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.i("====>", "RESULT_ERROR_NULL_PDU");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.i("====>", "RESULT_ERROR_RADIO_OFF");
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.i("====>", "RESULT_OK");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("=====>", "RESULT_CANCELED");
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager smsm = SmsManager.getDefault();
        smsm.sendTextMessage(number, null, message, sentPI, deliveredPI);
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
                    mBarcode = (String) msg.obj;
                    int boxNum = openEmptyCabinet();
                    if (boxNum == 0) {
                        Intent intent = new Intent(MainActivity.this, BarcodeFailActivity.class);
                        intent.putExtra("ErrorReason", "没有空箱");
                        startActivity(intent);
                    } else {
                        openDoor(boxNum);
                        Intent intent = new Intent(MainActivity.this, BarcodeOpenActivity.class);
                        intent.putExtra("BoxNum", boxNum);
                        startActivity(intent);
                    }
                } else if (msg.what == PHONENUM) {
                    String phoneNumber = (String) msg.obj;
                    int boxNumber = msg.arg1;
                    int password = generatePassword();

                    Data data = new Data();
                    data.setPassword(password);
                    data.setPhoneNumber(phoneNumber);
                    data.setBoxNumber(boxNumber);
                    data.setBarcode(mBarcode);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String str = formatter.format(curDate);
                    data.setRecordTime(str);
                    mData.add(data);

                    String string = "";
                    try {
                        string = mDomService.putData(mData);
                        writeFile("data.xml", string);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //sendShortMessage(boxNumber, password, phoneNumber);
                    Log.d(TAG, "SendMessage ...........................");
                }
            }
        }
    }

    private int openEmptyCabinet() {
        int[] flag = new int[Config.BOXNUMBER];
        for (Data data : mData) {
            int boxNumber = data.getBoxNumber();
            flag[boxNumber] = 1;
            Log.d(TAG, "flag[" + boxNumber + "] = " + flag[boxNumber]);
        }

        for (int i = 1; i < Config.BOXNUMBER; i++) {
            if (flag[i] != 1)
                return i;
        }
        return 0;
    }

    private int generatePassword() {
        int[] array = {0,1,2,3,4,5,6,7,8,9};
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        int result = 0;
        for(int i = 0; i < 6; i++)
            result = result * 10 + array[i];
        return result;
    }

    private void sendShortMessage(int boxNumber, int password, String phoneNumber) {
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        String message = generateSmsContent(boxNumber, password);
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
    }

    private String generateSmsContent(int boxNumber, int password) {
        return "你的包裹已经放在" + boxNumber + "号箱，开箱密码为：" + password + "，请尽快取出，谢谢。";
    }

    private int checkPassword(int password) {
        int boxNum;
        for (Data data : mData) {
            if (password == data.getPassword()) {
                boxNum = data.getBoxNumber();

                openDoor(boxNum);

                mData.remove(data);
                try {
                    writeFile("data.xml", mDomService.putData(mData));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return boxNum;
            }
        }
        return 0;
    }

    private void openDoor(int doorNo) {
/*        int fd;

        fd = Linuxc.openUart(Config.electronicLockDevice);
        if (fd < 0) {
            Log.e(TAG, "Hardware error");
            return;
        }
        Linuxc.setUart(Config.electronicLockBaudRate);

        Linuxc.sendHexUart(keyOfLock(doorNo));
        Linuxc.closeUart();

        if (Debug) Log.d(TAG, "The number of the door  " + doorNo + " is open!");*/
    }

    private int[] keyOfLock(int doorNo) {
        int[] key = {0x8A, 0x01, doorNo, 0x11, 0x9B};
        if (key[2] / 8 != 0)
            key[1] = key[2] / 8 + 1;

        key[4] = key[0] ^ key[1] ^ key[2] ^ key[3];
        return key;
    }

}
