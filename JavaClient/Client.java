import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class Client {

    private static String host;
    private static int port;

    public static void main(String... args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java XMLRPCClient <serverAddress> <serverPort>");
            System.exit(1);
        }

        serverAddress = args[0];
        serverPort = Integer.parseInt(args[1]);

        System.out.println(sum() == 0);
        System.out.println(sum(1, 2, 3, 4, 5) == 15);
        System.out.println(sum(2, 4) == 6);
        System.out.println(difference(12, 6) == 6);
        System.out.println(product(3, 4) == 12);
        System.out.println(product(1, 2, 3, 4, 5) == 120);
        System.out.println(quotient(10, 5) == 2);
        System.out.println(remainder(10, 5) == 0);
    }

    public static int sum(int... numbers) throws Exception {
        return sendRPCRequest("add", numbers);
    }

    public static int difference(int lhs, int rhs) throws Exception {
        return sendRPCRequest("subtract", lhs, rhs);
    }

    public static int product(int... numbers) throws Exception {
        return sendRPCRequest("multiply", numbers);
    }

    public static int quotient(int dividend, int divisor) throws Exception {
        return sendRPCRequest("divide", dividend, divisor);
    }

    public static int remainder(int dividend, int divisor) throws Exception {
        return sendRPCRequest("modulo", dividend, divisor);
    }

    private static int sendRPCRequest(String methodName, Object... params) throws Exception {
        try {
            // Create instance of client
            HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

            // Create a request body
            String parameters = "<params>";
            if (arguments.length == 0) {
                parameters += "<param><value><i4>" + 0 + "</i4></value></param>";
            } else {
                for (Object param : arguments) {
                    parameters += "<param><value><i4>" + (Integer) param + "</i4></value></param>";
                }
            }
            parameters += "</params>";

            String requestBody = "<?xml version = '1.0'?><methodCall><methodName>" + methodName + "</methodName>" + parameters + "</methodCall>";

            //  Send request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+host+":"+port+"/RPC"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "text/xml")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Object[] result = parseResponse(response.body());
            if (result.length == 1) {
                return Integer.valueOf((String)result[0]);
            } else {
                throw new ArithmeticException(result[0] + ", " + result[1]);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static Object[] parseXMLResponse(String responseBody) {
        Object[] result;
        // parse XML
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes());
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(bais);
            doc.getDocumentElement().normalize();

            if (doc.getDocumentElement().getElementsByTagName("fault").getLength() == 0) {
                result = new Object[1];
                result[0] = doc.getElementsByTagName("string").item(0).getTextContent();
                return result;
            } else {
                result = new Object[2];
                String fc = doc.getElementsByTagName("int").item(0).getTextContent();
                String fs = doc.getElementsByTagName("string").item(0).getTextContent();
                result[0] = fc;
                result[1] = fs;
                return result;
            }    
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
