package Parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public enum HttpState {
	REQUEST_LINE {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			
			System.out.println("REQUEST_LINE");
			
			boolean valid = true;
			
			String line = ParserUtils.readLine(buf);
			if(!line.isEmpty()){
				valid = ParserUtils.parseRequestLine(line, message);
				if(valid){
					return HEADER;
				}
			}
			return INVALID;
						
		}
	},
	HEADER {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			System.out.println("HEADER");
			String line = ParserUtils.readLine(buf);
			if(!line.isEmpty()){
				boolean valid = ParserUtils.parseHeaderLine(line, message);
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
			System.out.println("EMPTY LINE");				
			return BODY;
		}
	},
	BODY {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			System.out.println("BODY");
			String line = ParserUtils.readLine(buf);
			
			if(!line.isEmpty()){
				boolean valid = ParserUtils.parseBody(line, message);
				if(valid){
					return BODY;
				}else{
					return INVALID;
				}
			}
						
			return DONE;
		}
	},
	DONE {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			System.out.println("DONE");
			ParserUtils.doneReading(message);
			
			return DONE;
		}
	},
	INVALID {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message) {
			message.setValidMessage(false);
			return INVALID;
		}
	};
	
	protected abstract HttpState next(final BufferedReader buf, final HttpMessage message);
	
	public final HttpState process(final BufferedReader buf, final HttpMessage message) {
				
		HttpState current = this;
		 		
		try {
			do {

				current = current.next(buf, message);
				
				//TODO VER SI CON br.ready() alcanza!!
			} while (buf.ready() && current != DONE);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return current;
	}
	
	
}


