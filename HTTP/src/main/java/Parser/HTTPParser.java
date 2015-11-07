package Parser;



import CarlyProxy.ParserResponse;

public class HTTPParser {

	private HttpState state; 
	private HttpMessage message;
	
	public HTTPParser(){
		this.state = HttpState.REQUEST_LINE;
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
