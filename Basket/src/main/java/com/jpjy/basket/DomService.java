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
    private static final boolean Debug = Config.Debug;
    private static final String TAG = "DomService";

    public DomService() {
    }

    public List<Data> getDataResult(String dataString) throws Exception {
        List<Data> list;

        InputStream inputStream = new ByteArrayInputStream(dataString.getBytes());
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

            String recordTime = element.getAttribute("RecordTime");
            data.setRecordTime(recordTime);

            String phoneNumber = element.getAttribute("PhoneNumber");
            data.setPhoneNumber(phoneNumber);

            int password = Integer.parseInt(element.getAttribute("Password"));
            data.setPassword(password);

            int barcode = Integer.parseInt(element.getAttribute("Barcode"));
            data.setBoxNumber(barcode);

            int boxNumber = Integer.parseInt(element.getAttribute("BoxNumber"));
            data.setBoxNumber(boxNumber);

            list.add(data);
        }
        return list;
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public String putData(List<Data> list) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlVersion("1.0");

        Element Data = document.createElement("DataRecord");
        document.appendChild(Data);
        for (Data ignored : list) {
            Data.appendChild(document.createElement("Data"));
        }

        NodeList nodeList = document.getElementsByTagName("Data");
        for (int k = 0;
             k < nodeList.getLength(); k++) {
            Node node = nodeList.item(k);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elementNode = (Element) node;
                elementNode.setAttribute("RecordTime", list.get(k).getRecordTime());
                elementNode.setAttribute("PhoneNumber", String.valueOf(list.get(k).getPhoneNumber()));
                elementNode.setAttribute("password", String.valueOf(list.get(k).getPassword()));
                elementNode.setAttribute("barcode", String.valueOf(list.get(k).getBarcode()));
                elementNode.setAttribute("BoxNumber", String.valueOf(list.get(k).getBoxNumber()));
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


}
