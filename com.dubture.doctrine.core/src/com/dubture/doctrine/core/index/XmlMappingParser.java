package com.dubture.doctrine.core.index;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dubture.doctrine.core.model.Entity;

/**
 * Parse XML ORM mapping metadata.
 */
public class XmlMappingParser
{
    private XPath xPath;
    private Document doc;
    
    public XmlMappingParser(InputStream file) throws Exception
    {
        xPath = XPathFactory.newInstance().newXPath();
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    }

    public void parse() throws Exception
    {
        String servicePath = "/doctrine-mapping/entity";
        NodeList routeNodes = getNodes(servicePath);

        for (int i = 0; i < routeNodes.getLength(); i++) {
            Node node = routeNodes.item(i);             
            NamedNodeMap atts = node.getAttributes();
            String phpClass = null;

            for (int j = 0; j < atts.getLength(); j++) {

                Attr attr = (Attr) atts.item(j);
                String key = attr.getName();
                if (key.equals("repository-class")) {
				} else if (key.equals("name"))
                    phpClass = attr.getValue();

            }

            if (phpClass != null) {
                Entity entity = new Entity(null, phpClass);
                DoctrineBuilder.addPendingEntity(entity);
            }
        }
    }

    private NodeList getNodes(String path) throws Exception {

        XPathExpression xpathExpr = xPath.compile(path);
        Object result = xpathExpr.evaluate(doc,XPathConstants.NODESET);
        return (NodeList) result;

    }
}
