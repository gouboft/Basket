package com.jpjy.basket;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.apache.http.util.EncodingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

    public List<Data> getData(String responsedata) throws Exception {
        List<Data> list = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(responsedata);
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

    public int getUpload(String responsedata) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(responsedata);
        // 得到根元素，这里是 DataDownloadResponse
        Element root = document.getDocumentElement();
        // 得到一个集合，里面存放xml文件中所有的 Data
        NodeList nodeList = root.getElementsByTagName("DataUploadResponse");
        String result = root.getAttribute("Result");
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
                elementNode.setAttribute("TradeNo", list.get(k).getTradeNo());
                elementNode.setAttribute("BoxNo", String.valueOf(list.get(k).getBoxNo()));
                elementNode.setAttribute("FLAG", String.valueOf(list.get(k).getFLAG()));
            }
        }


        DOMSource domSource = new DOMSource(document);

        return domSource.toString();

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
            }
        }
        for (int m = 0; m < name.length; m++) {
            nodeList = document.getElementsByTagName(name[m]);
            node = nodeList.item(m);
//            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                element.appendChild(document.createTextNode(value[m]));
//            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        if (Debug) Log.d(TAG, domSource.toString());
        byte[] result = domSource.toString().getBytes("UTF-8");
        return android.util.Base64.encodeToString(result, Base64.DEFAULT);
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
            if (Debug) Log.d(TAG, domSource.toString());
            byte[] result = domSource.toString().getBytes("UTF-8");
            return android.util.Base64.encodeToString(result, Base64.DEFAULT);
        } else {
            byte[] result = upload.toString().getBytes("UTF-8");
            return android.util.Base64.encodeToString(result, Base64.DEFAULT);
        }
    }

    private String getTerminalNo() {
        //Todo: get this from the property
        return "20120001";
    }

    private String getLicenceNo() {
        //Todo: get this String from the filesystem
        return "ABCDEF-GHIJK-LMNOP-QRSTU";
    }
}
