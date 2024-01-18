package com.prj.chatgpt.infrastructure.util.sdk;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.*;

public class XmlUtil {

    /**
     * Parse the request sent by WeChat (xml)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> xmlToMap(HttpServletRequest request) throws Exception {
        // Get the input stream from request
        try (InputStream inputStream = request.getInputStream()) {
            // Store the parsed result in HashMap
            Map<String, String> map = new HashMap<>();
            // Read input stream
            SAXReader reader = new SAXReader();
            // Get xml documents
            Document document = reader.read(inputStream);
            // Get the root element of xml
            Element root = document.getRootElement();
            // Get all sub nodes of root element
            List<Element> elementList = root.elements();
            // Traverse all sub nodes
            for (Element e : elementList)
                map.put(e.getName(), e.getText());
            // Resource release
            inputStream.close();
            return map;
        }
    }

    private static void mapToXML2(Map map, StringBuffer sb) {
        Set set = map.keySet();
        for (Object o : set) {
            String key = (String) o;
            Object value = map.get(key);
            if (null == value)
                value = "";
            if (value.getClass().getName().equals("java.util.ArrayList")) {
                ArrayList list = (ArrayList) map.get(key);
                sb.append("<").append(key).append(">");
                for (Object o1 : list) {
                    HashMap hm = (HashMap) o1;
                    mapToXML2(hm, sb);
                }
                sb.append("</").append(key).append(">");

            } else {
                if (value instanceof HashMap) {
                    sb.append("<").append(key).append(">");
                    mapToXML2((HashMap) value, sb);
                    sb.append("</").append(key).append(">");
                } else {
                    sb.append("<").append(key).append("><![CDATA[").append(value).append("]]></").append(key).append(">");
                }

            }

        }
    }

    /**
     * Convert map to xml and response back to wechat server
     */
    static String mapToXML(Map map) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        mapToXML2(map, sb);
        sb.append("</xml>");
        try {
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
