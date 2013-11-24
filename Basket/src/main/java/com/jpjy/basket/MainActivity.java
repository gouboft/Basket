package com.jpjy.basket;


import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Intent;

import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final int PASSWORD = 0x0001;
    private static final int RFIDCARD = 0x0010;

    private MyApplication myApp;
    private DataPackage dp;
    private FileInputStream mInputData;
    private FileInputStream mInputUpload;
    private DomService domService;
    private boolean isDownload;
    private boolean isRun;
    private Thread mTransmitThread;
    private Thread mRfidThread;
    private EventHandler mEventHandler;
    private List<Data> mData;
    private List<Upload> mUpload;

    private FileInputStream mRfidCard;
    private byte[] mBufferRfid;
    private SerialPort mBarcode;
    private SerialPort mElecLock;
    private InputStream mBarcodeStream;
    private byte[] mBufferBarcode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApp = (MyApplication) getApplication();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, ChioceActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        }, 2000);

        mEventHandler = new EventHandler();
        myApp.setHandler(mEventHandler);

        try {
            mRfidCard = openFileInput("/dev/RFID");
            mBarcode = myApp.getBarcodeSerialPort();
            mBarcodeStream = mBarcode.getInputStream();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        domService = new DomService();

        isDownload = true;

        mTransmitThread = new TransmitThread();
        mTransmitThread.start();

        mRfidThread = new CardBarcodeThread();
        mRfidThread.start();
    }

    private DataPackage generateDataPack(DataPackage dp) {

        dp.setServiceName("dataDownLoadService");
        try {
            dp.setRequestData(domService.putRequestData(null));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dp;
    }

    private DataPackage generateUploadPack(DataPackage dp) {
        try {
            mInputUpload = openFileInput("Upload.xml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        dp.setServiceName("dataUpLoadService");
        try {
            dp.setRequestData(domService.putRequestData(mInputData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dp;
    }


    private class TransmitThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                isRun = true;
                dp = new DataPackage();
                if (isDownload) {
                    generateDataPack(dp);
                } else {
                    generateUploadPack(dp);
                }
                getRemoteInfo(dp);
                try {
                    isRun = false;
                    // Get the data one time in one minute;
                    sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CardBarcodeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    mBufferRfid = new byte[4];
                    //Todo: add barcode and then use poll or select

                    if (mBufferRfid != null)
                        mRfidCard.read(mBufferRfid);
                    Message msg = mEventHandler.obtainMessage(RFIDCARD, mBufferRfid.toString());
                    mEventHandler.sendMessage(msg);
                    //mBarcode.read(mBufferBarcode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final class EventHandler extends Handler {
        public void handlerMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PASSWORD) {
                int password = (Integer) msg.obj;
                checkPassword(password);
            } else if (msg.what == RFIDCARD) {
                String rfidCode = (String) msg.obj;
                checkRfidCode(rfidCode);
            }
        }
    }

    private boolean checkRfidCode(String rfidcode) {
        for (int i = 0; i < mData.size(); i++) {
            if (rfidcode == mData.get(i).getCardSN()) {
                int boxno = mData.get(i).getBoxNo();
                //Todo: Open the door of the boxno
                openDoor(boxno);
                return true;
            }
        }
        return false;
    }

    private boolean checkPassword(int password) {
        for (int i = 0; i < mData.size(); i++) {
            if (password == mData.get(i).getPassword()) {
                int boxno = mData.get(i).getBoxNo();
                //Todo: Open the door of the boxno
                openDoor(boxno);
                return true;
            }
        }
        return false;
    }

    private void openDoor(int doorNo) {
        //Todo: 实现这个函数
    }

    private void getRemoteInfo(DataPackage dp) {
        // 命名空间
        String nameSpace = "http://118.144.127.105:5005";
        // 调用的方法名称
        String methodName = "";
        // EndPoint
        String endPoint = "http://118.144.127.105:5005/BoxService/webservice/soap";
        // SOAP Action
        String soapAction = "http://118.144.127.105:505/dataDownLoadService";

        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(nameSpace, methodName);

        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        rpc.addProperty("serviceName", dp.getServiceName());
        rpc.addProperty("requestContext", dp.getRequestContext());
        rpc.addProperty("requestData", dp.getRequestData());

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);

        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
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
        int serviceResult = Integer.parseInt(object.getProperty("serviceResult").toString());
        if (serviceResult == 1) {
            dp.setResponseContext(object.getProperty("responseContext").toString());
            dp.setResponseData(object.getProperty("responseData").toString());
        }
    }


}
