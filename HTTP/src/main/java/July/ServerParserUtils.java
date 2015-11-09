package July;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import CarlyAdmin.manager.ConfigurationManager;
import Parser.HttpResponse;

public class ServerParserUtils {

	public static void processResponse(ByteBuffer buf, HttpResponse response) throws IOException{
		String line = null;
		boolean doneReadingStLine = false;
		boolean doneReadingHeaders = false;
		int size = -1;
		
		StateResponse state = response.getState();
		if(!state.getIsFinished()){
			switch(state.onMethod()){
			case 2:try{
					doneReadingHeaders = getHeaders(buf, response, state.getLastLine());
				}catch(Exception e){
					//temita con los headers
				}break;
			case 3:parseBody(buf, response, state.getQueue(), state.getOpenedTags());break;
			}
		}else{
			try{
				doneReadingStLine = parseResponseStatusLine(readLine(buf), response);
			}catch(Exception e){
				//TODO
			}
			try{
				doneReadingHeaders = getHeaders(buf, response, new StringBuilder());
			}catch(Exception e){
				//temita con los headers
			}
			//buf.flip();
			if(doneReadingHeaders && response.isPlainText() /*&& isLeetEnabled()*/){
				parseBody(buf, response, state.getQueue(), state.getOpenedTags());
			}
		}
		
	}
	
	private static boolean parseResponseStatusLine(String line, HttpResponse response) throws NumberFormatException, IOException{
		int item = 0;
		int last = line.indexOf(" ");
		int next = 0;
		String version = line.substring(0, last);
		next = last+1;
		last = line.indexOf(" ", next);
		String code = line.substring(next, last);
		int sCode = Integer.valueOf(code);
		String desc = line.substring(next, line.length());
		response.setVersion(version.substring(5, version.length()));
		response.setStatusCode(sCode);
		response.setStatusResponse(desc);

		return true;
	}
	
	
	private static boolean getHeaders(ByteBuffer buf, HttpResponse response, StringBuilder genLine) throws IOException{
		boolean doneReading = false;		
		//StringBuilder genLine = new StringBuilder();
		StateResponse state = response.getState();
		char c;
		int b;

		while(buf.hasRemaining() && !doneReading && (b = buf.get())!= -1){
			c = (char)b;
			if(c == '\n'){
				if(genLine.toString().trim().equals("")){
					doneReading = true;
				}else{
					setHeader(genLine.toString().trim(), response);
					genLine = new StringBuilder();
				}
			}else{
				genLine.append(c);
			}
		}
		if(!doneReading){
			state = response.getState();
			state.setIsFinished(false);
			state.setOnMethod(2);
			state.setLastLine(genLine);
		}else{
			state.setLastLine(null);
		}
		return doneReading;
	}
	
	private static void setHeader(String line, HttpResponse response){
		String h,v;
		h = line.substring(0, line.indexOf(":"));
		v = line.substring(line.indexOf(":")+2, line.length());
		response.getHeaders().put(h, v);
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
		}else if(h.equals("Connection")){
			response.closeConnection(v.equals("close"));
		}
	}
	
	private static boolean isLeetEnabled(){
		return ConfigurationManager.getInstance().isL33t();
	}
	
	private static boolean parseBody(ByteBuffer buf, final HttpResponse response, LinkedList<Character> queue, LinkedList<String> openedTags) throws IOException{
		StringBuilder resp = new StringBuilder();
		Set<String> tags = loadHtmlTags(); 
		char c;
		int b = 0;
		boolean finished = false;
		boolean setOnTag = false;
		
		while(buf.hasRemaining() && (b = buf.get()) != -1 && !finished){
			c = (char)b;
			switch(c){
				case '<': add2Queue(queue, c);break;
				case '>': getTag(response.getState(), queue, openedTags, tags);break;
				case ' ': if(inTag(queue, openedTags)){
							response.getState().setOnTag(true);
						}break;
				case '/': finishedTag(queue, c); break;
				default:  if(isLetter(c)){
							if(!response.getState().getOnTag() && !addLetterInQueue(queue, c)){
								c = applyLeet(c);
							}
						}break;
			}
			resp.append(c);
		}
		return finished;
	}
	
	private static void add2Queue(LinkedList<Character> queue, Character c){
		if(queue.isEmpty() || queue.getLast() == '>'){
			queue.addLast(c);
		}
	}
	
	private static void getTag(StateResponse status, LinkedList<Character> queue, LinkedList<String> openedTags, Set<String> tags){
		StringBuilder tag = new StringBuilder();
		String name = "";
		char c;
		if(queue.isEmpty() || queue.getLast() == '>'){
			if(!tags.contains(openedTags.getLast().toUpperCase())){
				openedTags.removeLast();
				status.setOnTag(false);
				return;
			}
		}
		
		while(!queue.isEmpty() && (c = queue.getLast()) != '<'){
			tag.append(c);
			queue.removeLast();
		}
		if(!queue.isEmpty()){
			queue.removeLast();
			name = tag.toString();
			tag = new StringBuilder();
			for(int i=name.length()-1; i>=0; i--){
				tag.append(name.charAt(i));
			}

			if(name.contains("/")){
				if(!openedTags.isEmpty() && openedTags.getLast().equals(tag.toString())){
					openedTags.removeLast();
				}
			}else{
				openedTags.addLast(tag.toString());
			}
		}
	}
	
	private static boolean inTag(LinkedList<Character> queue, LinkedList<String> openedTags){
		char c;
		if(!queue.isEmpty()){
			c = queue.getLast();
			if(isLetter(c)){
				//repite codigo
				StringBuilder tag = new StringBuilder();
				String name;
				while(!queue.isEmpty() && (c = queue.getLast()) != '<'){
					tag.append(c);
					queue.removeLast();
				}
				if(!queue.isEmpty()){
					queue.removeLast();
					name = tag.toString();
					tag = new StringBuilder();
					for(int i=name.length()-1; i>=0; i--){
						tag.append(name.charAt(i));
					}

					if(name.contains("/")){
						if(!openedTags.isEmpty() && openedTags.getLast().equals(tag.toString())){
							openedTags.removeLast();
						}
					}else{
						openedTags.addLast(tag.toString());
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static void finishedTag(LinkedList<Character> queue, char c){
		char last;
		if(!queue.isEmpty() && (last = queue.getLast()) == '<' && last != '>'){
			queue.addLast(c);
		}
	}
	
	private static boolean addLetterInQueue(LinkedList<Character> queue, char c){
		char last;
		if(!queue.isEmpty() && ((last = queue.getLast()) == '<' || isLetter(last))){
			queue.addLast(c);
			return true;
		}
		return false;
	}
	
	private static boolean isLetter(char c){
		return Character.isLetter(c);
	}
	
	private static char applyLeet(char c){
		char ret = c;
		if(isLeetEnabled()){
			switch(c){
				case 'a': ret = '4'; break;
				case 'e': ret = '3'; break;
				case 'i': ret = '1'; break;
				case 'o': ret = '0'; break;
				case 'c': ret = '<'; break;
			}
		}
		return ret;
	}
	/*private static void getNameTag(LinkedList<Character> queue, LinkedList<String> openedTags){
		char c;
		StringBuilder tag = new StringBuilder();
		if(!queue.isEmpty()){
			 c = queue.getLast();
			 if(c !='>'){
				 
			 }
		}
	}*/
	
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
		//buf.flip();
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
		buf.flip();
		return new String(array).trim();
	}
	
	private static Set<String> loadHtmlTags() {
		Set<String> ht = new HashSet<String>();
		ht.add("TITLE");
		ht.add("HTML");
		ht.add("HEAD");
		ht.add("BASE");
        ht.add("ISINDEX");
        ht.add("LINK");
        ht.add("BODY");
        ht.add("H1");
        ht.add("P");
        ht.add("H2");
        ht.add("H3");
        ht.add("H4");
        ht.add("H5");
        ht.add("H6");
        ht.add("PRE");
        ht.add("ADDRESS");
        ht.add("BLOCKQUOTE");
        ht.add("UL");
        ht.add("OL");
        ht.add("DIR");
        ht.add("MENU");
        ht.add("DL");
        ht.add("B");
        ht.add("I");
        ht.add("CITE");
        ht.add("CODE");
        ht.add("EM");
        ht.add("KBD");
        ht.add("SAMP");
        ht.add("STRONG");
        ht.add("VAR");
        ht.add("A");
        ht.add("FORM");
        ht.add("SELECT");
        ht.add("TEXTAREA");
        ht.add("LI");
        ht.add("OPTION");
    
        return ht;
	}
}
