package com.jpjy.basket;

/**
 * Created by link on 12/17/13.
 */
public class Data {
    private String phoneNumber;
    private int password;
    private int boxNumber;
    private String barcode;
    private String recordTime;

    public Data() {
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPassword(int password) {
        this.password = password;
    }

    public int getPassword() {
        return password;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }
    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public String getRecordTime() {
        return recordTime;
    }
}
