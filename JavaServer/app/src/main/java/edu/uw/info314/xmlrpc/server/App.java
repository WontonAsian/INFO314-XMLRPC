package edu.uw.info314.xmlrpc.server;


import java.io.StringReader;
import java.util.*;
import java.util.logging.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static spark.Spark.*;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());
    private static final int PORT = 8080;

    public static void main(String[] args) {
        port(PORT);
        LOG.info("Starting up on port " + PORT);

        before((req, res) -> {
            if (!req.uri().equals("/RPC")) {
                halt(404, "URL must be /RPC");
            }
            if (!req.requestMethod().equals("POST")) {
                halt(405, "Only POST is supported");
            }
        });

        post("/RPC", (req, res) -> {
            Call call = extractXMLRPCCall(req.body());
            String methodName = call.name;

            int[] params = new int[call.args.size()];
            for (int i = 0; i < params.length; i++) {
                params[i] = (int)call.args.get(i);
            }

            String result;
            Calc c = new Calc();
            switch (methodName) {
                case "add":
                    try {
                        result = createXMLResponse(c.add(params));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(2, "overflow");
                    }
                    break;
                case "subtract":
                    result = createXMLResponse(c.subtract(params[0], params[1]));
                    break;
                case "multiply":
                    try {
                        result = createXMLResponse(c.multiply(params));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(2, "overflow");
                    }
                    break;
                case "divide":
                    try {
                        result = createXMLResponse(c.divide(params[0], params[1]));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(1, "divide by zero");
                    }
                    break;
                case "modulo":
                    try {
                        result = createXMLResponse(c.modulo(params[0], params[1]));
                    } catch (ArithmeticException e) {
                        result = createFaultXMLResponse(1, "divide by zero");
                    }
                    break;
                default:
                    result = createFaultXMLResponse(4, "Method not supported");
                    break;
            }

            res.status(200);
            return result;
        });
    }

    private static String createXMLResponse(int result) {
        return String.format(
            "<?xml version=\"1.0\"?>\n" +
            "<methodResponse>\n" +
            "  <params>\n" +
            "    <param>\n" +
            "      <value><i4>%d</i4></value>\n" +
            "    </param>\n" +
            "  </params>\n" +
            "</methodResponse>", result);
    }

    private static String createFaultXMLResponse(int faultCode, String faultString) {
        return String.format(
            "<?xml version=\"1.0\"?>\n" +
            "<methodResponse>\n" +
            "  <fault>\n" +
            "    <value>\n" +
            "      <struct>\n" +
            "        <member>\n" +
            "          <name>faultCode</name>\n" +
            "          <value><i4>%d</i4></value>\n" +
            "        </member>\n" +
            "        <member>\n" +
            "          <name>faultString</name>\n" +
            "          <value><string>%s</string></value>\n" +
            "        </member>\n" +
            "      </struct>\n" +
            "    </value>\n" +
            "  </fault>\n" +
            "</methodResponse>", faultCode, faultString);
    }

    public static Call extractXMLRPCCall(String body) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(body)));
    
        String methodName = doc.getElementsByTagName("methodName").item(0).getTextContent();
    
        NodeList params = doc.getElementsByTagName("param");
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < params.getLength(); i++) {
            Node param = params.item(i);
            String value = param.getFirstChild().getTextContent();
            args.add(Integer.parseInt(value));  // Assume all arguments are integers
        }
    
        Call call = new Call();
        call.name = methodName;
        call.args = args;
        return call;
    }
}
