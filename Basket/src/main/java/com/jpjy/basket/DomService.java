package com.jpjy.basket;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DomService {
    private static final boolean Debug = true;
    private static final String TAG = "DomService";

    public DomService() {
    }

    public List<Data> getDataResult(String responsedata) throws Exception {
        List<Data> list = null;

        InputStream inputStream = new ByteArrayInputStream(responsedata.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        // 得到根元素，这里是 DataDownloadResponse
        Element root = document.getDocumentElement();
        // 得到一个集合，里面存放xml文件中所有的 Data
        NodeList nodeList = root.getElementsByTagName("Data");

        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        // 初始化
        list = new ArrayList<Data>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            // xml中的Data标签
            Element element = (Element) nodeList.item(i);
            Data data = new Data();

            String tradeNo = element.getAttribute("TradeNo");
            data.setTradeNo(tradeNo);

            String cardSN = element.getAttribute("CardSN");
            data.setCardSN(cardSN);

            int password = Integer.parseInt(element.getAttribute("Password"));
            data.setPassword(password);

            int boxNo = Integer.parseInt(element.getAttribute("BoxNo"));
            data.setBoxNo(boxNo);

            int flag = Integer.parseInt(element.getAttribute("FLAG"));
            data.setFLAG(flag);

            list.add(data);
        }
        return list;
    }

    public int getUploadResult(String responsedata) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream inputStream = new ByteArrayInputStream(responsedata.getBytes());
        Document document = builder.parse(inputStream);
        // 得到根元素，这里是 DataDownloadResponse
        Element root = document.getDocumentElement();
        String result = root.getAttribute("Result");

        if (Debug) Log.d(TAG, "getUpload result = " + result);
        return Integer.parseInt(result);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public String putUpload(List<Upload> list) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlVersion("1.0");

        Element Upload = document.createElement("DataUploadRequest");
        document.appendChild(Upload);
        for (int i = 0; i < list.size(); i++) {
            Upload upload = list.get(i);
            Upload.appendChild(document.createElement("Upload"));
        }

        NodeList nodeList = document.getElementsByTagName("Upload");
        for (int k = 0; k < nodeList.getLength(); k++) {
            Node node = nodeList.item(k);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elementNode = (Element) node;
                elementNode.setAttribute("OpenTime", list.get(k).getOpenTime());
                elementNode.setAttribute("OpenType", String.valueOf(list.get(k).getOpenType()));
                elementNode.setAttribute("OptCardNo", String.valueOf(list.get(k).getOptCardNo()));
                elementNode.setAttribute("TradeNo", list.get(k).getTradeNo());
                elementNode.setAttribute("password", String.valueOf(list.get(k).getPassword()));
                elementNode.setAttribute("BoxNo", String.valueOf(list.get(k).getBoxNo()));
                elementNode.setAttribute("FLAG", String.valueOf(list.get(k).getFLAG()));
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(domSource, result);
        if (Debug) Log.d(TAG, writer.toString());

        return writer.toString();
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public String putRequestContext() throws Exception {
        //requestContext
        String TerminalNo;
        String TerminalType;
        String LicenceNo;
        String name[] = {"TerminalNo", "TerminalType", "LicenceNo"};

        TerminalNo = getTerminalNo();
        TerminalType = "2";
        LicenceNo = getLicenceNo();
        String value[] = {TerminalNo, TerminalType, LicenceNo};

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlVersion("1.0");

        Element requestContext = document.createElement("RequestContext");
        document.appendChild(requestContext);

        requestContext.appendChild(document.createElement("Group"));
        NodeList nodeList = document.getElementsByTagName("Group");
        Node node = nodeList.item(0);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element elementNode = (Element) node;
            elementNode.setAttribute("name", "SystemInfo");
            elementNode.appendChild(document.createElement("Key"));
            elementNode.appendChild(document.createElement("Key"));
            elementNode.appendChild(document.createElement("Key"));
        }

        nodeList = document.getElementsByTagName("Key");
        for (int k = 0; k < nodeList.getLength(); k++) {
            node = nodeList.item(k);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                element.setAttribute("name", name[k]);
                element.setTextContent(value[k]);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.transform(domSource, result);
        if (Debug) Log.d(TAG, "requestContext = " + writer.toString());
        byte[] ret = writer.toString().getBytes("UTF-8");
        return android.util.Base64.encodeToString(ret, Base64.DEFAULT);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public String putRequestData(String upload) throws Exception {
        if (upload == null) {
            // Will never changed
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlVersion("1.0");

            Element requestContext = document.createElement("DataDownloadRequest");
            document.appendChild(requestContext);
            //Todo: check that Wheather the TerminalNo is the same with ContainerNo
            requestContext.setAttribute("ContainerNo", getTerminalNo());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(domSource, result);
            if (Debug) Log.d(TAG, "RequestData = " + writer.toString());
            byte[] ret = writer.toString().getBytes("UTF-8");
            return android.util.Base64.encodeToString(ret, Base64.DEFAULT);
        } else {
            if (Debug) Log.d(TAG, "RequestData = " + upload);
            byte[] result = upload.toString().getBytes("UTF-8");
            return android.util.Base64.encodeToString(result, Base64.DEFAULT);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public String getResponseContext(String responseContext) throws Exception {
        List<String> list = null;

        InputStream inputStream = new ByteArrayInputStream(responseContext.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        // 得到根元素，这里是 DataDownloadResponse
        Element root = document.getDocumentElement();
        // 得到一个集合，里面存放xml文件中所有的 Data
        NodeList nodeList = root.getElementsByTagName("ResponseMessage");

        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        // 初始化
        list = new ArrayList<String>();
        Element element = (Element) nodeList.item(0);
        String responseMesage = element.getTextContent();

        return responseMesage;
    }

    public List<Upload> getUpload(String uploaded) throws Exception {
        List<Upload> list = null;

        InputStream inputStream = new ByteArrayInputStream(uploaded.getBytes());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        // 得到根元素，这里是 DataDownloadResponse
        Element root = document.getDocumentElement();
        // 得到一个集合，里面存放xml文件中所有的 Data
        NodeList nodeList = root.getElementsByTagName("Upload");

        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        }
        // 初始化
        list = new ArrayList<Upload>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            // xml中的Data标签
            Element element = (Element) nodeList.item(i);
            Upload upload = new Upload();

            String tradeNo = element.getAttribute("TradeNo");
            upload.setTradeNo(tradeNo);

            list.add(upload);
        }
        return list;
    }
    private String getTerminalNo() {
        //Todo: get this from the property
        return "1029384756";
    }

    private String getLicenceNo() {
        //Todo: get this String from the filesystem
        return "ABCDEF-GHIJK-LMNOP-QRSTU";
    }
}
