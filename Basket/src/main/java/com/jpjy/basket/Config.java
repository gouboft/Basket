package com.jpjy.basket;

public class Config {
    public static final boolean Debug = false;

    public static final String serverEndPoint = "http://118.144.127.105:5005/Box_Service/webservice/soap";
    public static final String serverNameSpace = "http://soap.webservice.scaffold.goldenvista.com/";
    public static final String serverMethodName = "service";
    public static final String serverSoapAction = serverNameSpace + serverMethodName;

    public static final int DownloadInterval = 300000; //5 minutes
    public static final int UploadInterval = 300000;

    public static final String barcodeDevice = "/dev/ttyS6";
    public static final int barcodeBaudRate = 9600;
    public static final String electronicLockDevice = "/dev/ttyS7";
    public static final int electronicLockBaudRate = 9600;
    public static final String RFIDCardDevice = "/dev/rfid_rc522_dev";

    public static String getTerminalNo() {
        //Todo: get this from the property
        return "1029384756";
    }

    public static String getLicenceNo() {
        //Todo: get this String from the filesystem
        return "ABCDEF-GHIJK-LMNOP-QRSTU";
    }

    public static String getRFIDCardPassword() {
        //TODO implement this method
        return "";
    }

}
