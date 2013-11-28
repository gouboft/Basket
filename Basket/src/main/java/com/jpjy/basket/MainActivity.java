package com.jpjy.basket;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity {
    private static final boolean Debug = true;
    private static final String TAG = "MainActivity";

    private static final int PASSWORD = 0x0001;
    private static final int RFIDCARD = 0x0010;
    private static final int TRANSMIT = 0x0100;
    private static final int BARCODE = 0x1000;

    private MyApplication myApp;
    private DataPackage dp;

    private DomService domService;
    private boolean isDownload;

    private Thread mDownloadThread;
    private Thread mUploadThread;

    private EventHandler mEventHandler;
    private List<Data> mData;
    private List<Upload> mUpload;
    private int mWaitingFlag;
    private String passwordRecord;
    private String rfidcardRecord;
    private String barcodeRecord;

    private FileInputStream mRfidCard;
    private byte[] mBufferRfid;

    private InputStream mBarcodeStream;
    private byte[] mBufferBarcode;

    private boolean isNetworkConnect;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (MyApplication) getApplication();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);


        HandlerThread ht = new HandlerThread("EventHandler");
        ht.start();
        mEventHandler = new EventHandler(ht.getLooper());
        myApp.setHandler(mEventHandler);

        domService = new DomService();
        mUpload = new ArrayList<Upload>();
        mData = new ArrayList<Data>();
        isDownload = true;

        mDownloadThread = new DownloadThread();
        mDownloadThread.start();

        mUploadThread = new UploadThread();
        mUploadThread.start();

        try {
            mData = domService.getDataResult(readFile("data.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mUpload = new ArrayList<Upload>();
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
        mDownloadThread.interrupt();
        mDownloadThread = null;
        mUploadThread.interrupt();
        mUploadThread = null;
        finish();
    }

    private boolean checkNetworkInfo() {
        ConnectivityManager cm;
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //Todo: WIFI -> MOBILE

        State mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if (mobile.equals(null))
            return false;
        return mobile == State.CONNECTED;
    }

    public void writeFile(String fileName, String writeStr) throws IOException {
        try {
            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = writeStr.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFile(String fileName) throws IOException {
        String res = "";
        try {
            FileInputStream fin = openFileInput(fileName);

            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private void generateDataPack(DataPackage dp) {
        dp.setServiceName("dataDownLoadService");

        try {
            dp.setRequestContext(domService.putRequestContext());
            dp.setRequestData(domService.putRequestData(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateUploadPack(DataPackage dp) {
        dp.setServiceName("dataUpLoadService");

        try {
            String upload;
            upload = readFile("upload.xml");

            dp.setRequestContext(domService.putRequestContext());
            dp.setRequestData(domService.putRequestData(upload));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DownloadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if (Debug) Log.d(TAG, "DownloadThread running");
                //download
                isDownload = true;

                Message msg = mEventHandler.obtainMessage(TRANSMIT);
                mEventHandler.sendMessage(msg);

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UploadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if (Debug) Log.d(TAG, "UploadThread running");
                //upload
                isDownload = false;

                Message msg = mEventHandler.obtainMessage(TRANSMIT);
                mEventHandler.sendMessage(msg);

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isNumeric(String str) {
        for (int i = str.length();--i >= 0;) {
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
                        if (tag == 0) {
                            mWaitingFlag = PASSWORD;
                            isDownload = true;
                            Message msg1 = mEventHandler.obtainMessage(TRANSMIT);
                            mEventHandler.sendMessage(msg1);
                            return;
                        }

                        Intent intent = new Intent(MainActivity.this, PasswordFailActivity.class);
                        intent.putExtra("ErrorReason", "密码错误");
                        startActivity(intent);
                    }
                } else if (msg.what == RFIDCARD) {
                    if (Debug) Log.d(TAG, "Handle Rfid card");
                    String rfidCode = (String) msg.obj;
                    int tag = msg.arg1;
                    int boxNum = checkRfidCode(rfidCode);
                    if (boxNum > 0) {
                        Intent intent = new Intent(MainActivity.this, RfidcardOpenActivity.class);
                        intent.putExtra("BoxNum", boxNum);
                        startActivity(intent);
                    } else if (boxNum == 0) {
                        if (tag == 0) {
                            mWaitingFlag = RFIDCARD;
                            isDownload = true;
                            Message msg1 = mEventHandler.obtainMessage(TRANSMIT);
                            mEventHandler.sendMessage(msg1);
                            return;
                        }

                        Intent intent = new Intent(MainActivity.this, RfidcardFailActivity.class);
                        intent.putExtra("ErrorReason", "无效卡");
                        startActivity(intent);
                    }
                } else if (msg.what == BARCODE) {
                    if (Debug) Log.d(TAG, "Handle BarCode");
                    String barCode = (String) msg.obj;
                    int tag = msg.arg1;
                    //TODO Implement this function if need
                    //checkBarCode(barCode);
                    int boxNum = 01;
                    if (boxNum > 0) {

                        Intent intent = new Intent(MainActivity.this, BarcodeOpenActivity.class);
                        intent.putExtra("BoxNum", boxNum);
                        startActivity(intent);
                    } else if (boxNum == 0) {
                        if (tag == 0) {
                            mWaitingFlag = BARCODE;
                            isDownload = true;
                            Message msg1 = mEventHandler.obtainMessage(TRANSMIT);
                            mEventHandler.sendMessage(msg1);
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, BarcodeFailActivity.class);
                        intent.putExtra("ErrorReason", "无效条码");
                        startActivity(intent);
                    }
                } else if (msg.what == TRANSMIT) {
                    if (Debug) Log.d(TAG, "Handle Transmit");

                    dp = new DataPackage();
                    if (isDownload) {
                        generateDataPack(dp);
                    } else {
                        generateUploadPack(dp);

                        if (dp.getRequestData().equals("")) {
                            return;
                        }
                    }
                    if (checkNetworkInfo())
                        getRemoteInfo(dp);
                    else
                        return;

                    if (dp.getResponseData() == null)
                        return;

                    if (isDownload) {
                        byte resdata[] = android.util.Base64.decode(dp.getResponseData(),
                                Base64.DEFAULT);
                        try {
                            String data = new String(resdata, "UTF-8");
                            //Save the data to filesystem, will change every time
                            writeFile("data.xml", data);
                            mData = domService.getDataResult(data);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // recheck the information after we sync the data from the server
                        if (mWaitingFlag == PASSWORD) {
                            mWaitingFlag = 0;
                            //arg1 == 1, will not try again when password is incorrect
                            Message msg1 = mEventHandler.obtainMessage(PASSWORD, 1, 0, passwordRecord);
                            mEventHandler.sendMessage(msg1);

                        } else if (mWaitingFlag == RFIDCARD) {
                            mWaitingFlag = 0;
                            Message msg1 = mEventHandler.obtainMessage(RFIDCARD, 1, 0, rfidcardRecord);
                            mEventHandler.sendMessage(msg1);
                        } else if (mWaitingFlag == BARCODE) {
                            mWaitingFlag = 0;
                            Message msg1 = mEventHandler.obtainMessage(BARCODE, 1, 0, barcodeRecord);
                            mEventHandler.sendMessage(msg1);
                        }

                    } else {
                        String result = decodeBase64(dp.getResponseData());
                        try {
                            if (domService.getUploadResult(result) != 0) {
                                Log.e(TAG, "Error occur when upload data, Error code = " + result);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Upload success,remove the uploaded Upload in mUpload
                        try {
                            String ul = readFile("upload.xml");
                            writeFile("upload.xml", "");
                            List<Upload> uploadedList = domService.getUpload(ul);
                            for (Upload uploaded : uploadedList) {
                                for (Upload upload : mUpload)
                                    if (uploaded.getTradeNo().equals(upload.getTradeNo())) {
                                        mUpload.remove(upload);

                                    }
                            }
                            dumpUpload();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private int checkRfidCode(String rfidcode) {
        int boxNum;
        for (Data data : mData) {
            if (rfidcode.equals(data.getCardSN())) {
                boxNum = data.getBoxNo();

                openDoor(boxNum);

                // Record the passwordopen box data to filesystem
                mUpload.add(generateUpload(1, data));
                //Remove the data from the list because it is used
                mData.remove(data);
                try {
                    writeFile("upload.xml", domService.putUpload(mUpload));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return boxNum;
            }
        }
        rfidcardRecord = rfidcode;
        return 0;
    }

    private int checkPassword(int password) {
        dumpData();
        int boxNum;
        for (Data data : mData) {
            if (password == data.getPassword()) {
                boxNum = data.getBoxNo();

                openDoor(boxNum);

                // Record the passwordopen box data to filesystem
                mUpload.add(generateUpload(2, data));
                //Remove the data from the list because it is used
                mData.remove(data);
                try {
                    writeFile("upload.xml", domService.putUpload(mUpload));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return boxNum;
            }
        }
        passwordRecord = String.valueOf(password);
        return 0;
    }

    private Upload generateUpload(int openType, Data data) {
        Upload upload = new Upload();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        upload.setOpenTime(str);
        upload.setOpenType(openType);
        upload.setPassword(data.getPassword());
        upload.setOptCardNo(data.getCardSN());
        upload.setTradeNo(data.getTradeNo());
        upload.setBoxNo(data.getBoxNo());
        upload.setFLAG(data.getFLAG());
        return upload;
    }

    private void openDoor(int doorNo) {
        //Todo: 实现这个函数
        Log.d(TAG, "The number of the door  " + doorNo + " is open!");
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void getRemoteInfo(DataPackage dp) {
        // 命名空间
        String nameSpace = "http://soap.webservice.scaffold.goldenvista.com/";
        // 调用的方法名称
        String methodName = "service";
        // EndPoint
        String endPoint = "http://118.144.127.105:5005/Box_Service/webservice/soap";
        // SOAP Action
        String soapAction = "http://soap.webservice.scaffold.goldenvista.com/service";

        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(nameSpace, methodName);

        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        if (false) {
            Log.d(TAG, "$serviceName$ = " + dp.getServiceName());
            Log.d(TAG, "$requestContext$:" + decodeBase64(dp.getRequestContext()));
            Log.d(TAG, "$requestData$:" + decodeBase64(dp.getRequestData()));
        }
        rpc.addProperty("serviceName", dp.getServiceName());
        rpc.addProperty("requestContext", dp.getRequestContext());
        rpc.addProperty("requestData", dp.getRequestData());

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = false;
        // 等价于envelope.bodyOut = rpc;
        envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(endPoint);
        try {
            // 调用WebService
            transport.call(soapAction, envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取返回的数据
        SoapObject object = (SoapObject) envelope.bodyIn;
        // 获取返回的结果
        if (object == null) {
            Log.e(TAG, "Server Error");
            return;
        }

        String serviceResult = object.getPropertySafelyAsString("serviceResult");
        String responseContext = object.getPropertySafelyAsString("responseContext");
        String responseData = object.getPropertySafelyAsString("responseData");

        if (serviceResult == null) {
            Log.e(TAG, "Maybe Server Error, No serviceResult return!");
            return;
        }

        int sr = Integer.parseInt(serviceResult);
        if (sr == 0 && !responseContext.equals("")) {
            dp.setResponseContext(responseContext);
            if (Debug) Log.d(TAG, "ResponseContext = " + decodeBase64(responseContext));
        } else if (sr != 0 && !responseContext.equals("")) {
            // Server return a error, we print the error
            String string = decodeBase64(responseContext);
            try {
                Log.e(TAG, "Server return a error: " + domService.getResponseContext(string));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // No responseData return when server return a error
            return;
        }

        if (!responseData.equals("")) {
            dp.setResponseData(responseData);
            if (Debug) Log.d(TAG, "ResponseData = " + decodeBase64(responseData));
        } else {
            Log.e(TAG, "Error, no ResponseData return");
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private String decodeBase64(String string) {
        byte[] result = android.util.Base64.decode(string, Base64.DEFAULT);
        String string1 = null;
        try {
            string1 = new String(result, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return string1;
    }


    private void dumpData() {
        if (mData.equals(""))
            return;
        Log.d(TAG, "The number mData have ------- " + mData.size() + " ------- Data");
        for (int i = 0; i < mData.size(); i++) {
            Data data = mData.get(i);

            Log.d(TAG, "mData[" + i + "]:");
            Log.d(TAG, "\tTradeNo\t" + data.getTradeNo());
            Log.d(TAG, "\tFLAG\t" + data.getFLAG());
        }
    }

    private void dumpUpload() {
        if (mUpload.equals(""))
            return;
        Log.d(TAG, "The number of mUpload have ------- " + mUpload.size() + " ------- Upload");
        for (int i = 0; i < mUpload.size(); i++) {
            Upload upload = mUpload.get(i);

            Log.d(TAG, "mUpload[" + i + "]:");
            Log.d(TAG, "\tTradeNo\t" + upload.getTradeNo());
        }
    }

}
