package July;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import CarlyAdmin.manager.ConfigurationManager;
import Parser.HttpResponse;

public class ServerParserUtils {

	public static void processResponse(ByteBuffer buf, HttpResponse response) throws IOException{
		String line = null;
		boolean doneReadingStLine = false;
		boolean doneReadingHeaders = false;
		int size = -1;
		
		try{
			doneReadingStLine = parseResponseStatusLine(readLine(buf), response);
		}catch(Exception e){
			//TODO
		}
		
		
		try{
			doneReadingHeaders = getHeaders(buf, response);
		}catch(Exception e){
			//temita con los headers
		}
		
		if(response.getStatusCode() == 200 && response.isPlainText() && isLeetEnabled()){
			parseBody(buf, response);
		}
		
	}
	
	private static boolean parseResponseStatusLine(String line, HttpResponse response) throws NumberFormatException, IOException{
		int item = 0;
		StringBuilder word = null;
		char c;
		int b;
		boolean doneReading = false;
		
		word = new StringBuilder();
		while((b = string.read())!= -1 && (c = (char)b) != '\n'){
			if(c == ' '){
				if(item<3){
					item++;
					switch(item){
						case 1: response.setVersion(getVersion(word.toString())); 
							word = new StringBuilder();
							break;
						case 2: response.setStatusCode(Integer.valueOf(word.toString()));
							word = new StringBuilder();
							break;
					}
				}else{
					word.append(c);
				}
			}else{
				word.append(c);
			}
		}
		if(item == 3){
			response.setStatusResponse(word.toString());
			doneReading = true;
		}
		return doneReading;
	}
	
	
	private static boolean getHeaders(final BufferedReader buf, final HttpResponse response) throws IOException{
		String line = null;
		boolean doneReading = false;
		
		Map<String, String> headers = new HashMap<String, String>();
		
		String h,v;
		while(buf.ready() && (line = buf.readLine())!=null && !line.isEmpty()){
			h = line.substring(0, line.indexOf(":"));
			v = line.substring(line.indexOf(":")+1, line.length()-1);
			headers.put(h, v);
			//set variables
			if(h.equals("Content-Length")){
				response.setLength(Integer.valueOf(v));
			}else if(h.equals("Content-Type")){
				response.setPlainText(v.contains("text/html"));
				response.setgZip(v.contains("gzip"));
			}else if(h.equals("Transfer-Encoding")){
				response.setgZip(v.contains("gzip"));
			}
			else if(h.equals("Content-Encoding")){
				response.setgZip(v.contains("gzip"));
			}
		}
		if(line.isEmpty()){
			doneReading = true;
		}
		return doneReading;
	}
	
	private static boolean isLeetEnabled(){
		return ConfigurationManager.getInstance().isL33t();
	}
	
	private static boolean parseBody(final BufferedReader buf, final HttpResponse response) throws IOException{
		String title;
		String body;
		StringBuilder resp = new StringBuilder();
		char c;
		int b;
		boolean finished = false;
		
		while(buf.ready() && (b = buf.read()) != -1){
			c = (char)b;
			resp.append(c);
		}
		finished = true;
		return finished;
	}
	
	private static String getVersion(String serverVersion){
		String v = "";
		v = serverVersion.substring(serverVersion.indexOf("/")+1, serverVersion.length());
		return v;
	}
	
	public static String readLine(ByteBuffer buf) {
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
		return new String(array).trim();
	}
}
