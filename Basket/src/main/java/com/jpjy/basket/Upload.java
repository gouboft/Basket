package com.jpjy.basket;

/**
 * Created by bo on 11/22/13.
 */
public class Upload {
    private String OpenTime;
    private int OpenType;
    private String TradeNo;
    private int BoxNo;
    private int FLAG;

    public Upload(String OpenTime, int OpenType, String TradeNo, int BoxNo, int FLAG) {
        super();
        this.TradeNo = OpenTime;
        this.OpenType = OpenType;
        this.TradeNo = TradeNo;
        this.BoxNo = BoxNo;
        this.FLAG = FLAG;
    }

    public Upload() {
        super();
    }

    // OpenTime
    public String getOpenTime() {
        return OpenTime;
    }
    public void setOpenTime (String OpenTime) {
        this.OpenTime = OpenTime;
    }

    // OpenType
    public int getOpenType() {
        return OpenType;
    }
    public void setOpenType (int OpenType) {
        this.OpenType = OpenType;
    }

    // TradeNo
    public String getTradeNo() {
        return TradeNo;
    }
    public void setTradeNo (String TradeNo) {
        this.TradeNo = TradeNo;
    }

    // BoxNo
    public int getBoxNo() {
        return BoxNo;
    }
    public void setBoxNo (int BoxNo) {
        this.BoxNo = BoxNo;
    }

    // FLAG
    public int getFLAG() {
        return FLAG;
    }
    public void setFLAG (int FLAG) {
        this.FLAG = FLAG;
    }


}
