package com.jpjy.basket;

/**
 * Created by bo on 11/22/13.
 */
public class Upload {
    private String OpenTime;
    private int OpenType;
    private int BoxNo;
    private String OptCardNo;
    private String TradeNo;
    private int Password;
    private int FLAG;

    public Upload() {
    }

    // OpenTime
    public String getOpenTime() {
        return OpenTime;
    }
    public void setOpenTime(String OpenTime) {
        this.OpenTime = OpenTime;
    }

    // OpenType
    public int getOpenType() {
        return OpenType;
    }
    public void setOpenType(int OpenType) {
        this.OpenType = OpenType;
    }

    // BoxNo
    public int getBoxNo() {
        return BoxNo;
    }
    public void setBoxNo(int BoxNo) {
        this.BoxNo = BoxNo;
    }

    // OptCardNo
    public String getOptCardNo() {
        return OptCardNo;
    }
    public void setOptCardNo(String OptCardNo) {
        this.OptCardNo = OptCardNo;
    }

    // TradeNo
    public String getTradeNo() {
        return TradeNo;
    }
    public void setTradeNo(String TradeNo) {
        this.TradeNo = TradeNo;
    }

    // Password
    public int getPassword() {
        return Password;
    }
    public void setPassword(int Password) {
        this.Password = Password;
    }

    // FLAG
    public int getFLAG() {
        return FLAG;
    }
    public void setFLAG(int FLAG) {
        this.FLAG = FLAG;
    }


}
