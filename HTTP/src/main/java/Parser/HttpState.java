package Parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public enum HttpState {
	REQUEST_LINE {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			
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
						
			return BODY;
		}
	},
	BODY {
		@Override
		protected HttpState next(final BufferedReader buf, final HttpMessage message){
			
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
			
			ParserUtils.doneReading(message);
			
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


