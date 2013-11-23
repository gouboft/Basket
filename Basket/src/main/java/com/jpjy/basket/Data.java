package com.jpjy.basket;

/**
 * Created by bo on 11/22/13.
 */
public class Data {
    private String TradeNo;
    private String CardSN;
    private int Password;
    private int BoxNo;
    private int FLAG;

    public Data(String TradeNo, String CardSN, int Password, int BoxNo, int FLAG) {
        super();
        this.TradeNo = TradeNo;
        this.CardSN = CardSN;
        this.Password = Password;
        this.BoxNo = BoxNo;
        this.FLAG = FLAG;
    }

    public Data() {
        super();
    }

    // TradeNo
    public String getTradeNo() {
        return TradeNo;
    }
    public void setTradeNo (String TradeNo) {
        this.TradeNo = TradeNo;
    }
    // CardSN
    public String getCardSN() {
        return CardSN;
    }
    public void setCardSN (String CardSN) {
        this.CardSN = CardSN;
    }
    // Password
    public int getPassword() {
        return Password;
    }
    public void setPassword (int Password) {
        this.Password = Password;
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
