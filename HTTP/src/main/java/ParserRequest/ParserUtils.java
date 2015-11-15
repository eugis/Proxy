package ParserRequest;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;

public class ParserUtils {
	
	private static Logger logs = CarlyLogger.getCarlyLogger();
	
	private static final Set<String> validMethods = loadMethods();
	private static final Set<String> validRequestHeaders = loadRequestHeaders();
	private static final Set<String> validGeneralHeaders = loadGeneralHeaders();
	
	private static Set<String> loadMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		headers.add("HEAD");
		return headers;
	}
	
	private static Set<String> loadRequestHeaders() {
		Set<String> rh = new HashSet<String>();
		rh.add("Accept");
		rh.add("Accept-Charset");
		rh.add("Accept-Encoding");
		rh.add("Accept-Language");
        rh.add("Authorization");
        rh.add("Expect");
        rh.add("From");
        rh.add("Host");
        rh.add("If-Match");
        rh.add("If-Modified-Since");
        rh.add("If-None-Match");
        rh.add("If-Range");
        rh.add("If-Unmodified-Since");
        rh.add("Max-Forwards");
        rh.add("Proxy-Authorization");
        //rh.add("Proxy-Connection");
        rh.add("Range");
        rh.add("Referer");
        rh.add("TE");
        rh.add("User-Agent");
        return rh;
	}
	
	private static Set<String> loadGeneralHeaders() {
		Set<String> rh = new HashSet<String>();
		rh.add("Cache-Control");
		rh.add("Connection");
        rh.add("Content-Length");
		rh.add("Date");
		rh.add("Pragma");
        rh.add("Trailer");
        rh.add("Transfer-Encoding");
        rh.add("Upgrade");
        rh.add("Via");
        rh.add("Warning");
        return rh;
	}

	public static String readLine(ByteBuffer buf, HttpMessage message) {
		boolean crFlag = false;
		boolean lfFlag = false;
		if(buf.limit() == 0){
			return null;
		}
		byte[] array = new byte[buf.limit()];
		int i = 0;
		byte b;
		buf.flip();
		do{
			b = buf.get();
			array[i++] = b;
			if(b != 0){
				message.buffer.put(message.pos, b);
				message.pos++;
			}
 
//			if(message.isCrFlag() && b != '\n'){
//				message.setcrFlag(false);
//			}
			
			if(b == '\r'){
//				if(message.isHeaderFinished()){
					message.setcrFlag(true);
//				}
				crFlag = true;
			}else if(b == '\n'){
				if(message.isLfFlag()){
					message.setFinished();
				}
//				if(message.isHeaderFinished()){
					message.setlfFlag(true);
//				}
				lfFlag = true;
				if(i == 1){ //quiere decir q viene solo un \n
					String emptyLine = "\n";
					return emptyLine;
				}
			}else{
				message.setlfFlag(false);
				message.setcrFlag(false);
			}
		}while(buf.hasRemaining() && !crFlag && !lfFlag);
		if(!crFlag && !lfFlag){
			return null;
		}else{
			if(crFlag){
				if(buf.limit() == 0 ||
						buf.limit() == buf.position()){
					return null;
				}
				b = buf.get();
				if(b != '\n'){
					return null;
				}
				array[i] = b;
				
				message.buffer.put(message.pos, (byte)'\n');
				message.pos++;
				
			}
		}
		buf.compact();
		int pos = buf.position();
		buf.limit(pos);
		
		
		
		
		return new String(array);//.trim();
	}
	
	public static boolean setHeaders(ByteBuffer buf, HttpMessage message, StringBuilder genLine) {
		boolean doneReading = false;		
		char c;
		byte b;
		buf.flip();
		
		while(buf.hasRemaining() && !doneReading && (b = buf.get())!= -1 && b != 0){
			c = (char)b;
			message.buffer.put(message.pos, b);
			message.pos++;
			
			if(c == '\n'){
				if(genLine.toString().trim().equals("")){
					doneReading = true;
				}else{
					parseHeaders(genLine.toString().trim(), message);
					genLine = new StringBuilder();
				}
			}else{
				genLine.append(c);
			}
		}
		if(!doneReading){
			message.setLastLine(genLine);
		}else{
			message.setLastLine(null);
		}
		
		return doneReading;
	}
	
	/**
	 * 
	 * @param line
	 * @param message
	 * @return 
	 */
	public static RequestLine parseMethod(String line, HttpMessage message) {
		String[] requestLine = line.split("\\s");
		RequestLine resp = RequestLine.ERROR;
		
		if(requestLine.length != 3){
			return RequestLine.ERROR;
		}
		if(isValidMethod(requestLine[0])){
			
			if(isValidURL(requestLine[1])){

				if(isValidVersion(requestLine[2])){
					resp = RequestLine.OK;
					message.setMethod(requestLine[0]);
					int index = requestLine[2].indexOf("/");
					String version = requestLine[2].substring(index+1, requestLine[2].length());
					message.setVersion(version);
				}else{
					resp = RequestLine.INVALIDVERSION;
				}
			}else{
				resp = RequestLine.INVALIDURL;
			}
		}else{
			resp = RequestLine.INVALIDMETHOD;
		}
		
		return resp;
	}

	private static boolean isValidVersion(String version) {
		String regex = "HTTP/1.(1|0)";
		Pattern patt = Pattern.compile(regex);
        Matcher matcher = patt.matcher(version);
        return matcher.matches();
	}

	private static boolean isValidURL(String url) {
//		System.out.println("isValidURL: " +  (url != null && url.length() > 0));
		return url != null && url.length() > 0;
	}

	private static boolean isValidMethod(String method) {
		return validMethods.contains(method);
	}

	public static boolean parseHeaders(String line, HttpMessage message) {
		boolean valid = false;
		int index;
		
		if(line.equals('\n')){
			message.setHeaderFinished(true);
			return valid;
		}
		
		index = line.indexOf(':');
		if(index < 0){
			//no esta bien formado el header
			logs.error("Request: The header field is not well formed");
			return false;
		}else{
			String header = line.substring(0, index); /*.toLowerCase();*/
			String value = line.substring(index+1).trim();
			if(validHeader(header) && validValue(value)){
				valid = message.addHeader(header, value);
			}else{
				logs.error("invalid header");
			}
		}
		
		return valid;
	}

	private static boolean validValue(String value) {
//		System.out.println("valid value: "+ value != null && value.length() > 0);
		return value != null && value.length() > 0;
	}

	public static boolean parseData(ByteBuffer buf, HttpMessage message) {
		if(message.bodyEnable()){
			String bytes = message.getHeader("Content-Length");
			if(bytes != null){
				if(!message.initBody()){
					message.setInitBody(message.pos);
				}
				//TODO hacer un metodo que vaya leyendo el buffer
				ParserUtils.readLine(buf, message);
				Integer cantbytes = Integer.parseInt(bytes);
				if(message.getReadBody() >= cantbytes){
					//TODO fijarse si hay que hacer algo mas
					//validar que venga bien el final del msje
					return true;
				}
			}
		}/*TODO else{
			ParserUtils.readLine(buf, message);
			//Descomentar para forzar la finalizacion del request
//			message.setFinished();
			return message.isFinished();
		}*/
		
		return false;
	}

	static boolean validHeader(String header) {
	//	if (header.contains(":")) {
	//		String[] headerParts = header.split(":");
//			System.out.println("isValidHeader: " + (validRequestHeaders.contains(header) || validGeneralHeaders.contains(header)));
			return validRequestHeaders.contains(header) || validGeneralHeaders.contains(header);
	//	}
	//	return false;
	}

	public static boolean minHeaders(HttpMessage message) {
		// validar que esten los headers necesarios: host, content-length,...Tener en cuenta que si es un GET no hay body!!
		boolean valid = true;
		if(message.getHost() == null){
			logs.error("missing host");
			message.setNoHost(true);
			valid = false;
		}
		
		if(!message.bodyEnable()){
			if(message.getMethod().equals("POST")){
				logs.error("length required");
				message.setNoContentLength(true);
				valid = false;
			}
		}
		return valid;
	}
	
	public static void concatBuffer(ByteBuffer buf, HttpMessage message){
		ByteBuffer aux = ByteBuffer.allocate(message.buffer.position() +
				buf.position());
		buf.flip();
		message.buffer.flip();
		aux.put(message.buffer);
		aux.put(buf);
		message.buffer = aux;
//		message.buffer.compact();
	}
	
	public static void printBuffer(ByteBuffer buf){
//		buf.flip();	
		byte[] array = new byte[buf.limit()];
		int i = 0;
		byte b;
		do{
			b = buf.get();
			System.out.println(String.valueOf(b));
			array[i++] = b;		
		}while(buf.hasRemaining());
	}
	
}
