package com.prj.chatgpt.infrastructure.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.commons.lang3.StringUtils;

import java.io.Writer;

/**
 *@description WeChat official account sends messages and parses tool class
 */
public class XmlUtil {
    //xstream extension, bean converted to xml automatically added! [CDATA[]]
    public static XStream getMyXStream() {
        return new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out) {
                    //Add CDATA tags to all xml nodes
                    boolean cdata = true;

                    @Override
                    public void startNode(String name, Class clazz) {
                        super.startNode(name, clazz);
                    }

                    @Override
                    protected void writeText(QuickWriter writer, String text) {
                        if (cdata && !StringUtils.isNumeric(text)) {
                            writer.write("<![CDATA[");
                            writer.write(text);
                            writer.write("]]>");
                        } else {
                            writer.write(text);
                        }
                    }
                };
            }
        });
    }

    /**
     * Bean converted to WeChat’s xml message format
     */
    public static String beanToXml(Object object) {
        XStream xStream = getMyXStream();
        xStream.alias("xml", object.getClass());
        xStream.processAnnotations(object.getClass());
        String xml = xStream.toXML(object);
        if (!StringUtils.isEmpty(xml)) {
            return xml;
        } else {
            return null;
        }
    }

    /**
     * Convert xml to bean generic method
     */
    public static <T> T xmlToBean(String resultXml, Class clazz) {
        // The XStream object sets default security protection and sets allowed classes.
        XStream stream = new XStream(new DomDriver());
        XStream.setupDefaultSecurity(stream);
        stream.allowTypes(new Class[]{clazz});
        stream.processAnnotations(new Class[]{clazz});
        stream.setMode(XStream.NO_REFERENCES);
        stream.alias("xml", clazz);
        return (T) stream.fromXML(resultXml);
    }
}
