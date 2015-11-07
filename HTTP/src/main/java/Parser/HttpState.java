package Parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public enum HttpState {
	STATUS_LINE {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			
			boolean valid = true;
			
			String line = ParserUtils.readLine(buf);
			if(!line.isEmpty()){
				valid = ParserUtils.parseMethod(line, message);
				if(valid){
					return HEADER;
				}
			}
			return INVALID;
			
			/*TODO fijarse donde poner este metodo
			int i = pos;
			String sbuf = new String( buf, Charset.forName("UTF-8") );
			char c = sbuf.charAt(i);
			StringBuilder aux = new StringBuilder(); 
						
			while( c != '\n'){
				
				c = sbuf.charAt(i);
				
				if(c != ' ' && message.hasMethod()){
					aux.append(c);
					
				}
				
				
				
				i++;
				
			}
			
			
			return new State(HEADER,i);*/
		}
	},
	HEADER {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			// TODO Auto-generated method stub
			// if (moreHeadersToCome) {
			//	return HEADER;
			//}
			
			String line = ParserUtils.readLine(buf);
			if(!line.isEmpty()){
				boolean valid = ParserUtils.parseHeaders(line, message);
				if(valid){
					return HEADER;
				}else{
					return INVALID;
				}
			}
			return EMPTY_LINE;
		}
	},
	EMPTY_LINE {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message) {
			// TODO Auto-generated method stub
			
			return BODY;
		}
	},
	BODY {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			// TODO Auto-generated method stub
			return DONE;
		}
	},
	DONE {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			
			return DONE;
		}
	},
	INVALID {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message) {
			
			return INVALID;
		}
	};
	
	protected abstract HttpState next(final BufferedReader buf, final HttpMessage message);
	
	public final HttpState process(final byte[] buf, final HttpMessage message) {
		
		
		HttpState current = this;
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(buf));
		BufferedReader br = new BufferedReader(in); 
		 		
		try {
			do {

				current = current.next(br, message);
				
				//TODO VER SI CON br.ready() alcanza!!
			} while (br.ready() && current != DONE);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return current;
	}
	
	
}


