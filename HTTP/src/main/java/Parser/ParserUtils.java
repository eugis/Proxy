package Parser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import CarlyAdmin.manager.ConfigurationManager;

public class ParserUtils {
	
	private static final Set<String> validMethods = loadMethods();
	private static Map<Integer, String> statusCode = loadStatusCode();
	private static Map<Integer, String> msgs = loadMsgs();
	
	public static boolean isLeetEnabled(){
		return ConfigurationManager.getInstance().isL33t();
	}
	
	private static Map<Integer, String> loadMsgs() {
		Map<Integer, String> msg = new HashMap<Integer, String>();
		msg.put(405, "The server supports only the following method: GET, POST and HEAD");
		return msg;
	}

	private static Set<String> loadMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		headers.add("HEAD");
		return headers;
	}
	
	private static Map<Integer, String> loadStatusCode() {
		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(100, "Continue");
		result.put(200, "OK");
		result.put(201, "Created");
		result.put(202, "Accepted");
		result.put(203, "Non-Authoritative Information");
		result.put(204, "No Content");
		result.put(205, "Reset Content");
		result.put(206, "Partial Content");
		result.put(300, "Multiple Choices");
		result.put(301, "Moved Permanently");
		result.put(302, "Found");
		result.put(303, "See Other");
		result.put(304, "Not Modified");
		result.put(305, "Not Modified");
		result.put(306, "(Unused)");
		result.put(307, "Temporary Redirect");
		result.put(400, "Bad Request");
		result.put(401, "Unauthorized");
		result.put(402, "Payment Required");
		result.put(403, "Forbidden");
		result.put(404, "Not Found");
		result.put(405, "Method Not Allowed");
		result.put(406, "Not Acceptable");
		result.put(407, "Proxy Authentication Required");
		result.put(408, "Request Timeout");
		result.put(409, "Conflict");
		result.put(410, "Gone");
		result.put(411, "Length Required");
		result.put(412, "Precondition Failed");
		result.put(413, "Request Entity Too Large");
		result.put(414, "Request-URI Too Long");
		result.put(415, "Request-URI Too Long");
		result.put(416, "Requested Range Not Satisfiable");
		result.put(417, "Expectation Failed");
		result.put(500, "Internal Server Error");
		result.put(501, "Not Implemented");
		result.put(502, "Bad Gateway");
		result.put(503, "Service Unavailable");
		result.put(504, "Gateway Timeout");
		result.put(505, "Http Version Not Supported");
		return result;
	}

	public static String toLeet(String text){
		if(isLeetEnabled()){
			text = text.replace('a', '4');
			text = text.replace('e', '3');
			text = text.replace('i', '1');
			text = text.replace('o', '0');
			text = text.replace('c', '<');
		}
		return text;
	}
	
	public static boolean isValidMethod(String method){
		return validMethods.contains(method);
	}

	public static String generateHttpResponseIM(String version){
		String aux = "";
		Integer sCode = 405;
		String firstLine = generateFirstLine(version, aux, sCode);
		String dataLine = generateHTMLData(sCode);
		Map<String,String> headerLine = generateHeaders(dataLine.length());
		
		aux += firstLine + printHeaders(headerLine) + dataLine;
		
		return aux;
	}

	private static String generateHTMLData(Integer error) {
		String html = "";
		
		html = "<html><body>";
		html += "<h1>" + error + ": " + statusCode.get(error) + "</h1>";
		html += msgs.get(error);
		html += "</body></html>";
		
		return html;
	}

	private static Map<String,String> generateHeaders(int contentLength) {
		Map<String,String> headers = new HashMap<String, String>();
		headers.put("Date",
				new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		headers.put("Content-Type", "text/html");
		headers.put("Content-Length", String.valueOf(contentLength));
		return headers;
	}

	private static String generateFirstLine(String version, String aux,
			Integer sCode) {
		//HTTP/1.0 200 OK
		aux += "HTTP/" + version + " " + sCode + " " + statusCode.get(sCode) + "\n";
		return aux;
	}
	
	private static String printHeaders(Map<String,String> headers){
		String headersLine = "";
		for (Entry<String, String> mapElement : headers.entrySet()) {
			headersLine += mapElement.getKey() + ": " + mapElement.getValue()
					+ "\n";
		}
		headersLine += "\n";
		return headersLine;
	}
	
	//Prueba
//	public static void main(String[] args) {
//		System.out.println(ParserUtils.generateHttpResponseIM("1.0"));
//	}

}
