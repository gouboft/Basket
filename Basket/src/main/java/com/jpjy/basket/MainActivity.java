package com.jpjy.basket;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.os.HandlerThread;

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

import android.content.Intent;

import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final boolean Debug = true;
    private static final String TAG = "MainActivity";

    private static final int PASSWORD = 0x0001;
    private static final int RFIDCARD = 0x0010;
    private static final int TRANSMIT = 0x0100;

    private MyApplication myApp;
    private DataPackage dp;

    private DomService domService;
    private boolean isDownload;

    private Thread mTransmitThread;

    private EventHandler mEventHandler;
    private List<Data> mData;
    private List<Upload> mUpload;

    private FileInputStream mRfidCard;
    private byte[] mBufferRfid;
    private SerialPort mBarcode;
    private SerialPort mElecLock;
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
        isDownload = true;

        mTransmitThread = new TransmitThread();
        mTransmitThread.start();


        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, ChoiceActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        }, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTransmitThread.interrupt();
        mTransmitThread = null;
    }

    private boolean checkNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(mobile == State.CONNECTED)
            return true;
        else return false;
    }
    public void writeFile(String fileName, String writestr) throws IOException{
        if(Debug) Log.d(TAG, "writeFile: " + fileName + "\n" + writestr);
        try{
            FileOutputStream fout =openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);
            fout.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }

    public String readFile(String fileName) throws IOException {

        String res = new String();
        try{
            FileInputStream fin = openFileInput(fileName);
            if (fin == null)
                return null;
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        if(Debug) Log.d(TAG, "readFile: " + fileName + "\n" + res);
        return res;
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
        dp.setServiceName("dataUpLoadService");

        try {
            String upload = readFile("upload.xml");
            if (upload == null)
                return null;
            dp.setRequestData(domService.putRequestData(upload));
        } catch (IOException e) {
            e.printStackTrace();
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

                if (isDownload)
                    isDownload = false;
                else
                    isDownload = true;

                Message msg = mEventHandler.obtainMessage(TRANSMIT);
                mEventHandler.sendMessage(msg);
                if(Debug) Log.d(TAG, "TransmitThread running");
                try {
                    // Get the data one time in one minute;
                    sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final class EventHandler extends Handler {
        public EventHandler() {
        }

        public EventHandler(Looper looper) {
            super(looper);
        }

        @TargetApi(Build.VERSION_CODES.FROYO)
        @Override
        public void handleMessage(Message msg) {
            if(Debug) Log.d(TAG, "Handle Message");
            super.handleMessage(msg);
            if (msg.what == PASSWORD) {
                if(Debug) Log.d(TAG, "Handle Password");
                int password = (Integer) msg.obj;

                if (checkPassword(password)) {
                    Intent intent = new Intent(MainActivity.this, OpenActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, PasswordFailActivity.class);
                    intent.putExtra("ErrorReason", "密码错误");
                    startActivity(intent);
                }
            } else if (msg.what == RFIDCARD) {
                if(Debug) Log.d(TAG, "Handle Rfid card");
                String rfidCode = (String) msg.obj;
                if(checkRfidCode(rfidCode)) {
                    Intent intent = new Intent(MainActivity.this, OpActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, CardFailActivity.class);
                    intent.putExtra("ErrorReason", "无效卡");
                    startActivity(intent);
                }
            } else if (msg.what == TRANSMIT) {
                if(Debug) Log.d(TAG, "Handle Transmit");
                synchronized(this) {
                    dp = new DataPackage();
                    if (isDownload) {
                        generateDataPack(dp);
                    } else {
                        if (generateUploadPack(dp) == null)
                            return;
                    }
                    if(checkNetworkInfo())
                        getRemoteInfo(dp);

                    if (isDownload) {
                        if (Debug) Log.d(TAG, "Downloading Data");
                        byte resdata[]=android.util.Base64.decode(dp.getResponseData(), Base64.DEFAULT);
                        try {
                            String data = new String(resdata, "UTF-8");
                            /*String data = readFile("upload.xml");*/
                            //Save the data to filesystem, will change every time
                            //writeFile("data.xml", data);
                            data = readFile("data.xml");
                            mData = domService.getData(data);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (Debug) Log.d(TAG, "Uploading Data");
                        byte resdata[]=android.util.Base64.decode(dp.getResponseData(), Base64.DEFAULT);
                        try {
                            String data = new String(resdata, "UTF-8");
                            int result = domService.getUpload(data);
                            if (result != 0) {
                                Log.d(TAG, "Error occur when upload data, Error code = " + result);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private boolean checkRfidCode(String rfidcode) {
        for (int i = 0; i < mData.size(); i++) {
            Data data = mData.get(i);
            if (rfidcode == data.getCardSN()) {
                int boxno = data.getBoxNo();
                int flag = data.getFLAG();
                if (flag == 0)
                    return false;
                else
                    data.setFLAG(0);

                openDoor(boxno);

                // Record the open box data to filesystem
                Upload upload = new Upload();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());
                String str = formatter.format(curDate);
                upload.setOpenTime(str);
                upload.setOpenType(1);
                upload.setTradeNo(data.getTradeNo());
                upload.setBoxNo(data.getBoxNo());
                mUpload.add(upload);
                try {
                    writeFile("upload.xml", domService.putUpload(mUpload));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                Message msg = mEventHandler.obtainMessage(TRANSMIT);
                mEventHandler.sendMessage(msg);
                msg = mEventHandler.obtainMessage(TRANSMIT, rfidcode);
                mEventHandler.sendMessage(msg);
            }
        }
        return false;
    }

    private boolean checkPassword(int password) {
        for (int i = 0; i < mData.size(); i++) {
            Data data = mData.get(i);
            if (password == data.getPassword()) {
                int boxno = data.getBoxNo();
                // Set the door open flag had been open
                int flag = data.getFLAG();
                if (flag == 0)
                    return false;
                else
                    data.setFLAG(0);

                openDoor(boxno);

                // Record the open box data to filesystem
                Upload upload = new Upload();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());
                String str = formatter.format(curDate);
                upload.setOpenTime(str);
                upload.setOpenType(2);
                upload.setTradeNo(data.getTradeNo());
                upload.setBoxNo(data.getBoxNo());
                mUpload.add(upload);
                try {
                    writeFile("upload.xml", domService.putUpload(mUpload));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                Message msg = mEventHandler.obtainMessage(TRANSMIT);
                mEventHandler.sendMessage(msg);
                msg = mEventHandler.obtainMessage(TRANSMIT, password);
                mEventHandler.sendMessage(msg);
            }
        }
        return false;
    }

    private void openDoor(int doorNo) {
        //Todo: 实现这个函数
        Log.d(TAG, "The Door which the No. is " + doorNo + "is open!");
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
        if (object == null)
            return;
        String ret = object.getProperty("serviceResult").toString();
        int serviceResult = 0;
        Log.d(TAG, "web service return : " + ret);
        if (ret.length() > 0)
            serviceResult = Integer.parseInt(ret);
        if (serviceResult == 1) {
            dp.setResponseContext(object.getProperty("responseContext").toString());
            dp.setResponseData(object.getProperty("responseData").toString());
        }
    }


}
