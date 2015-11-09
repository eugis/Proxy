package Parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class HTTPParser {

	private HttpState state; 
	private HttpMessage message;
//	private HttpResponse serverResponse;
	
	public HTTPParser(){
		this.state = HttpState.REQUEST_LINE;
		this.message = new HttpMessage();
//		this.serverResponse = new HttpResponse();
	}
	
	public ParserResponse sendData(byte[] buf, boolean client) {
		
		if(client){
			return parse(buf);
		}
		return parseServer(buf);
	}
	
	private ParserResponse parse(byte[] buf){
		
		ParserResponse response = new ParserResponse();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(buf));
		BufferedReader br = new BufferedReader(in); 
		
		state = state.process(br, message);
				
		
		try {
			//Si br no está ready significa que no hay más para leer.
			if(state!=HttpState.DONE && !br.ready()){
				message.setDoneReading(false);
			}
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		if(state == HttpState.INVALID){
		//TODO ver que casos tengo
		}
		
		response.populate(message);
		
		return response;
	}
	
	private ParserResponse parseServer(byte[] buf){
		ParserResponse response = new ParserResponse();
		InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(buf));
		BufferedReader br = new BufferedReader(in); 
		
//		try {
//			ParserUtils.processResponse(br, serverResponse);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
		return response;
	}

}
