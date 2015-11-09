package Parser2;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import CarlyAdmin.manager.ConfigurationManager;

public class ParserUtils {
	
	private static final Set<String> validMethods = loadMethods();
	private static final Set<String> validRequestHeaders = loadRequestHeaders();
	
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
        rh.add("Range");
        rh.add("Referer");
        rh.add("TE");
        rh.add("User-Agent");
        return rh;
	}
	
	public static boolean isLeetEnabled(){
		return ConfigurationManager.getInstance().isL33t();

	}

	public static String readLine(byte[] buf, HttpMessage message) {
		String ret=null;
		int init = message.getPosRead();
		int fin;
		
		for(int i=init; i < buf.length; i++){
			if(buf[i] == '\n'){
				fin = i+1;
				message.setPosRead(fin);
				ret = new String(buf, init, fin);
				return ret.trim();
			}
		}
		return ret;
	}

	public static String readLine(ByteBuffer buf, HttpMessage message) {
		// TODO revisar este metodo
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
			if(b == '\r'){
				crFlag = true;
			}else if(b == '\n'){
				lfFlag = true;
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
			}
		}
		buf.compact();
		int pos = buf.position();
		buf.limit(pos);
		return new String(array);//.trim();
	}

	public static boolean parseMethod(String line, HttpMessage message) {
		String[] requestLine = line.split("\\s");
		boolean valid = false;
		
		if(requestLine.length != 3){
			return false;
		}
		if(isValidMethod(requestLine[0])){
			message.setMethod(requestLine[0]);
			//TODO completar este metodo
			
			if(isValidURL(requestLine[1])){

				if(isValidVersion(requestLine[2])){
					valid = true;
				}
			}
		}
		
		return valid;
	}

	private static boolean isValidVersion(String string) {
		// TODO Auto-generated method stub
		return true;
	}

	private static boolean isValidURL(String string) {
		// TODO Auto-generated method stub
		return true;
	}

	private static boolean isValidMethod(String method) {
		return validMethods.contains(method);
	}

	public static boolean parseHeaders(String line, HttpMessage message) {
		// TODO Auto-generated method stub
		return true;
	}

	public static boolean parseData(ByteBuffer buf, HttpMessage message) {
		//TODO no estoy teniendo en cuenta que le buf puede venir partido
		//me parece q en HttpMessage habria q guardarse una instancia de buffer
		String bytes = message.getHeader("content-length");
		Integer cantbytes = Integer.parseInt(bytes);
		if(buf.capacity() >= cantbytes){
			//TODO fijarse si hay que hacer algo mas
			//validar que venga bien el final del msje
			return true;
		}
		return false;
	}

	public static boolean validHeader(String header) {
		return validRequestHeaders.contains(header);
	}

}
