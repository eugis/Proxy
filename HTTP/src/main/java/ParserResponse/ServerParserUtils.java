package ParserResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import CarlyAdmin.manager.ConfigurationManager;

public class ServerParserUtils {

	public static byte[] processResponse(ByteBuffer buf, HttpResponse response) throws IOException{
		boolean doneReadingStLine = false;
		boolean doneReadingHeaders = false;
		boolean doneReading = false;

//		if (!isLeetEnabled()) {
//			//response.setBuf(buf.array());
//			return buf.array();
//		}else{
			ByteBuffer respBuf = ByteBuffer.allocate(buf.capacity());
			
			StateResponse state = response.getState();
			
			if(!state.getIsFinished()){
				switch(state.onMethod()){

				case HEADERS:
					
					try{
						doneReadingHeaders = getHeaders(buf, response, state.getLastLine(), respBuf);
					}catch(Exception e){
						//temita con los headers
					}
					break;
					
				case BODY:
					
					parseBody(buf, response, state.getOpenedTags(), respBuf);
					break;

				}
			}else{
				try{
					doneReadingStLine = parseResponseStatusLine(buf, response, new StringBuilder(), respBuf);
				}catch(Exception e){
					//TODO
				}
				try{
					if(doneReadingStLine){
						doneReadingHeaders = getHeaders(buf, response, new StringBuilder(), respBuf);
					}
				}catch(Exception e){
					//temita con los headers
				}
				//buf.flip();
				if(doneReadingHeaders && response.isPlainText() /*&& isLeetEnabled()*/){
					doneReading = parseBody(buf, response, state.getOpenedTags(), respBuf);

					if(!doneReading){
						state.setOnMethod(State.BODY);
						state.setIsFinished(false);
					}else{
						state.setIsFinished(true);
					}
				}
			}
			return respBuf.array();
//		}
	}
	
	private static boolean parseResponseStatusLine(ByteBuffer buf, HttpResponse response, StringBuilder genLine, ByteBuffer respBuf) throws NumberFormatException, IOException{
		boolean doneReading = false;
		StateResponse state = response.getState();
		char c;
		int b;
		
		while(buf.hasRemaining() && !doneReading && (b = buf.get())!= -1){
			c = (char)b;
			respBuf.put((byte) b);
			if(c == '\n'){
				doneReading = parseStLine(genLine.toString().trim(), response);
			}else{
				genLine.append(c);
			}
		}
		if(!doneReading){
			state = response.getState();
			state.setIsFinished(false);
			state.setOnMethod(State.STATUS_LINE);
			state.setLastLine(genLine);
		}
		return doneReading;
	}
	
	private static boolean parseStLine(String line, HttpResponse response){
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
		
	private static boolean getHeaders(ByteBuffer buf, HttpResponse response, StringBuilder genLine, ByteBuffer respBuf) throws IOException{
		boolean doneReading = false;		
		//StringBuilder genLine = new StringBuilder();
		StateResponse state = response.getState();
		char c;
		int b;

		while(buf.hasRemaining() && !doneReading && (b = buf.get())!= -1){
			c = (char)b;
			respBuf.put((byte) b);
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
			state.setOnMethod(State.HEADERS);
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
	
	private static boolean parseBody(ByteBuffer buf, final HttpResponse response, LinkedList<String> openedTags, ByteBuffer respBuf) throws IOException{

		
		System.out.println("Entra parseBody");
		StringBuilder resp = new StringBuilder();
		Set<String> tags = loadHtmlTags();
		Set<String> voidElements = loadHtmlVoidElements();
		LinkedList<Character> queue = response.getState().getQueue();
		char c;
		boolean addLetter;
		int b = 0;
		int lenghtRead = 0;
		boolean finished = false;
		boolean onComment = response.getState().getOnComment();
		boolean voidElement = false;
		boolean rawElement = false;
		while(buf.hasRemaining() && (b = buf.get()) != -1 && !finished){
			String string = new String(buf.array());
			System.out.println(string);
			if(b == 0){
				finished = true;
			}else{
				c = (char)b;
				
								
				if (isLeetEnabled()) {
					switch(c){
					case '<': add2Queue(queue, c);
								if(rawElement){
									rawElement = false;
								}
								break;
					case '>': 
						
						//Ej: <meta ...atributos...\> o también <meta ...atributos...> 
						if(voidElement && onComment){
							emptyQueue(queue);
							onComment = false;
							voidElement = false;
						}
						//Si no es void element pero tiene atributos. Ej: <style ...atributos...> cosas q no quiero leetear </style>
						//Ponemos los tags con atributos dentro de las openTags. El endTag se trata como cualquier otro caso en el else.
						else if(onComment){
							String queueContent = putIntoOpenTags(queue, openedTags, tags);
							
							//RawHtmlElements: style, script. Para que no aplique leet a su contenido.
							if(queueContent != null && isRawElement(queueContent)){
								rawElement = true;
							}
							onComment = false;
						}
						
						//No hay atributos ni es un voidElement
						else{
									//Saca los endTags que no sean voidElements.
									getTag(response.getState(), queue, openedTags, tags);
						}
						break;
					case ' ':
						if(!onComment){
							onComment = addSpace2Queue(queue, c);
							
							if(onComment){
								System.out.println();
							}
							
							if(isVoidElement(queue, voidElements) && onComment){
								voidElement = true;
							}
															
						}
						break;
						
					case '/': 
						/* - Si la queue está vacía y hay un '/' es porque estoy en algún link, texto entre tags.
						 * - Como la pila queda llena cuando estoy en atributos de un voidElement y ellos pueden 
						 * 	 contener en su descripción una '/' también se agrega la condición onComment.
						 */
						if(!queue.isEmpty()&& !onComment){
							finishedTag(queue, c);
						}
						 
					break;
					
					default: 
						
						
						
						if(!onComment){
								onComment = onComment(queue, c);
												
																
								addLetter = addLetterInQueue(queue, c);
								
								if(!onComment && isLetter(c) && !addLetter && !queue.contains('/') && !rawElement){
									c = applyLeet(c);
								}
							  }
					break;
					}
				
				} else {
					lenghtRead ++;
					if (lenghtRead ==  response.getLength()) {
						response.getState().setIsFinished(true);
						response.getState().setOnMethod(State.BODY);
						finished = true;
					}
				}
				response.getState().setOnComment(onComment);
				resp.append(c);
				
				
				respBuf.put((byte)c);
				
				
			} 

		}
		
		System.out.println(resp.toString());
		return finished;
	}
	
	private static boolean isRawElement(String queueContent) {
		String q = queueContent.trim().toLowerCase();
		if(q.equals("style") || q.equals("script")){
			return true;
		}
		return false;
	}

	private static void emptyQueue(LinkedList<Character> queue) {
		
		while(!queue.isEmpty() && (queue.getLast()) != '<'){
			queue.removeLast();
		}
				
		queue.removeLast();//Me borra el <
		
	}

	private static boolean isVoidElement(LinkedList<Character> queue, Set<String> voidElements) {
		// TODO Auto-generated method stub
		String tag = readQueue(queue);
		
		if(voidElements.contains(tag.toLowerCase())){
			return true;
		}
			
		return false;
	}

	private static String readQueue(LinkedList<Character> queue){
		StringBuilder tag = new StringBuilder();
		String ret;
		char c;
		int i;
		
		
		if(!queue.isEmpty())
		{
			i = queue.size()-1;
			while((c = queue.get(i)) != '<'){
				tag.append(c);
				i--;
			}
			ret = tag.reverse().toString().trim();
			
			if(ret != null && !ret.isEmpty()){
				return ret;
			}
		}
		
		
		return null;
	}
	
	private static String putIntoOpenTags(LinkedList<Character> queue, LinkedList<String> openedTags, Set<String> tags){
		
		StringBuilder tag = new StringBuilder();
		String name = "";
		char c;
		
		//Se guarda en tag lo ultimo que quedó en la pila. Que coincide con el último tag abierto.
		while(!queue.isEmpty() && (c = queue.getLast()) != '<'){
			tag.append(c);
			queue.removeLast();
		}
		
		if(!queue.isEmpty()){
			queue.removeLast();//saca el < de la queue
			name = tag.reverse().toString().trim();
						
			if(!name.contains("/") && tags.contains(name.toUpperCase())){
				openedTags.addLast(name);
				return name;
			}
					
			
			
		}
		return null;
	}
	
	
	private static void add2Queue(LinkedList<Character> queue, Character c){
		if(queue.isEmpty()){
			queue.addLast(c);
		}
	}
	
	private static boolean addSpace2Queue(LinkedList<Character> queue, Character c){
		if(!queue.isEmpty() && isLetter(queue.getLast())){
			queue.addLast(c);
			return true;
		}
		return false;
	}
	
	private static void getTag(StateResponse status, LinkedList<Character> queue, LinkedList<String> openedTags, Set<String> tags){
		StringBuilder tag = new StringBuilder();
		String name = "";
		char c;
		if(queue.isEmpty() || queue.getLast() == '>'){
			/*if(!tags.contains(openedTags.getLast().toUpperCase())){
				openedTags.removeLast();
				status.setOnTag(false);
				return;
			}*/
			return;
		}
		
		//Se guarda en tag lo ultimo que quedó en la pila. Que coincide con el último tag abierto.
		while(!queue.isEmpty() && (c = queue.getLast()) != '<'){
			tag.append(c);
			queue.removeLast();
		}
		if(!queue.isEmpty()){
			queue.removeLast();//saca el < de la queue
			name = tag.reverse().toString().trim();
			
								
			//Existe el caso: </style' '> incluso con </HTML' '>
			if(name.contains("/")){
				String subs = name.substring(name.indexOf('/')+1);
				if(!openedTags.isEmpty() && openedTags.getLast().equals(subs)){
					openedTags.removeLast();
				}
								
			}else{
				if(!tags.contains(name.toUpperCase())){
					openedTags.removeLast();
					
				}
				openedTags.addLast(tag.toString());
			}
		}
	}
	
	private static boolean onComment(LinkedList<Character> queue, char c){
		char last;
		if(!queue.isEmpty()){
			last = queue.getLast();
			if(last == '<' && c != ' ' && !isLetter(c) && c!='!'){
				return true;
			}
			/*if(isLetter(c)){
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
			}*/
		}
		return false;
	}
	
	private static void finishedTag(LinkedList<Character> queue, char c){
		char last;
		if(!queue.isEmpty() && ((last = queue.getLast()) == '<' || last == ' ')){
			if(last == ' '){
				queue.removeLast();
			}
			queue.addLast(c);
		}
	}
	
	private static boolean addLetterInQueue(LinkedList<Character> queue, char c){
		char last;
		
		if(!queue.isEmpty()){
			last = queue.getLast();
			if(c!='!' && (last == '<' || last == '/' || isLetter(last))){
					queue.addLast(c);
					return true;
			}
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
	
	//TODO si necesitas identificar una linea con un enter fijate como lo hice en ParserUtils
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
	
	private static Set<String> loadHtmlVoidElements(){
		Set<String> ht = new HashSet<String>();
		
		ht.add("area");
		ht.add("base");
		ht.add("br");
		ht.add("col");
		ht.add("command");
		ht.add("embed");
		ht.add("hr");
		ht.add("img");
		ht.add("input");
		ht.add("keygen");
		ht.add("link");
		ht.add("meta");
		ht.add("param");
		ht.add("source");
		ht.add("track");		
		ht.add("wbr");
		ht.add("doctype");
		
		return ht;
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
        ht.add("STYLE");
        ht.add("SCRIPT");
        
            
        return ht;
	}
}
