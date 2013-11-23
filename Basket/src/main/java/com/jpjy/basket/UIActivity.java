package com.jpjy.basket;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.annotation.TargetApi;
import android.util.Base64;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Android平台调用WebService（手机号码归属地查询）
 *
 * @author liufeng
 * @date 2011-05-18
 */
public class UIActivity extends Activity {
    private static final String TAG = "UIActivity";

    private DataPackage dp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private DataPackage generateDataPack (Data data) {
        DataPackage dp;
        dp = new DataPackage();

        dp.setServiceName("dataDownLoadService");
        dp.setRequestData(data);

        return dp;
    }

    private DataPackage generateUploadPack (Upload upload) {
        DataPackage dp;
        dp = new DataPackage();

        dp.setServiceName("dataUpLoadService");
        dp.setRequestData(upload);

        return dp;
    }
    Runnable runnable = new Runnable(){
        @Override
        public void run() {
            getRemoteInfo(dp);
        }
    };

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