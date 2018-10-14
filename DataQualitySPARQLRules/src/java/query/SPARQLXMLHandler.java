/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author karsten
 * no changes made
 */
public class SPARQLXMLHandler extends DefaultHandler {

    boolean variables = false;
    boolean result = false;
    boolean uri_el = false;
    boolean literal = false;
    boolean bnode = false;

    private StringBuffer sb;

    int numofresults = 0;
    ArrayList result_list = new ArrayList();

    int numofvariables = 0;
    ArrayList<String> variable_list = new ArrayList();

    public StringBuffer getSb() {
        return sb;
    }

    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub
        super.startDocument();

        sb = new StringBuffer("<table class=\"table table-striped\">");
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        super.startElement(uri, localName, qName, attributes);
        if (localName.equals("head")) {
            sb.append(" <tr> ");
        }
        if (localName.equals("variable")) {
            sb.append(" <th>").append(attributes.getValue("name")).append("</th>");
        }
        if (localName.equals("result")) {
            sb.append("<tr> ");
        }

        if (localName.equals("binding")) {
            sb.append("<td> ");
        }
        if (localName.equals("uri")) {
            uri_el = true;
        }
        if (localName.equals("literal")) {
            literal = true;
        }
        if (localName.equals("bnode")) {
            bnode = true;
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (uri_el) {
            try {
                super.characters(ch, start, length);
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String s = new String(ch, start, length);
            sb.append(" <a href=\"").append(s).append("\">").append(s).append("</a> ");
            
        }
        if (literal) {
           sb.append(ch, start, length);
        }
        if (bnode) {
           sb.append("_:").append(ch, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // TODO Auto-generated method stub
        // building the html table
        sb.append(" </table>");
        super.endDocument();
        
    }

    /**
     * where the real stuff happens
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // TODO Auto-generated method stub
        //super.endElement(arg0, arg1, arg2);
        if (localName.equals("uri")) {
            uri_el = false;
        }
        if (localName.equals("literal")) {
            literal = false;
        }
        if (localName.equals("bnode")) {
            bnode = false;
        }
        if (localName.equals("result")) {
            sb.append(" </tr> ");
        }
        if (localName.equals("binding")) {
            sb.append("</td> ");
        }
        if (localName.equals("head")) {
            sb.append(" </tr> ");
        }
    }
}
