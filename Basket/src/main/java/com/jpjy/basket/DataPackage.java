package com.jpjy.basket;

/**
 * Created by bo on 11/22/13.
 */

public class DataPackage {

    private String serviceName;
    private String requestContext;
    private String requestData;
    private String responseContext;
    private String responseData;
    private DomService dm;

    public DataPackage() {
        dm = new DomService();
        setRequestContext();
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setRequestContext() {
        try {
            this.requestContext = dm.putRequestContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRequestContext() {
        return responseContext;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setResponseContext(String responseContext) {
        this.responseContext = responseContext;
    }

    public String getResponseContext() {
        return requestData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getResponseData() {
        return requestData;
    }

}
