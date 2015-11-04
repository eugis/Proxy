package Parser;

import java.nio.charset.Charset;

import CarlyProxy.ParserResponse;

public class HTTPParser {

	private HttpState state; 
	private HttpMessage message;
	
	public HTTPParser(){
		this.state = HttpState.STATUS_LINE;
		this.message = new HttpMessage();
	}
	
	public ParserResponse sendData(byte[] buf) {
		
		
		parse(buf);		
						
		//TODO: retornar httpmsg directo?
		return new ParserResponse();
	}
	
	private void parse(byte[] buf){
		
		
		
		state = state.process(buf, message);
		
				
	}
	
		
	

}
