package ParserRequest;

import java.nio.ByteBuffer;

import ParserRequest.ParserUtils;

public enum StateHttp {
	
	REQUEST_LINE {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			String line = ParserUtils.readLine(buf, message);
			if(line == null){
				return this;
			}
			RequestLine valid = ParserUtils.parseMethod(line.trim(), message);
			switch (valid) {
			case OK:
				message.state = HEADER;
				return message.state.process(buf, message);
			case INVALIDVERSION: case INVALIDURL: case ERROR:
				return INVALID;
			}
			
			return INVALIDMETHOD;
		}
		
	},
	HEADER {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			boolean finishedReading = ParserUtils.setHeaders(buf, message, message.getLastLine());
//				String line = ParserUtils.readLine(buf, message);
//				if(line == null){
//					return this;
//				}
//				if(!line.equals("\n")){
//					boolean valid = ParserUtils.parseHeaders(line.trim(), message);
//					if(!valid){
//						return INVALID;
//					}
//				}else{
			if(finishedReading){
				message.setHeaderFinished(true);
				if(!ParserUtils.minHeaders(message)){
					return INVALID;
				}else{
					message.state = EMPTY_LINE;
					return message.state.process(buf, message);
				}
			}
//				}
			return this;
		}
		
	},
	EMPTY_LINE {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			if(message.getMethod().equals("POST")){
				message.state = BODY;
				return message.state.process(buf, message);
			}else{
				message.state = DONE;
				return message.state.process(buf, message);
			}
		}
		
	},
	BODY {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			boolean finished = ParserUtils.parseData(buf, message);
			if(finished){
				message.state = DONE;
				return message.state.process(buf, message);
			}
			return this;
		}
		
	},
	DONE {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			//agrego las lineas de cierre al message
//			message.closeRequest();
			return this;
		}
		
	},
	INVALID {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			// hay que seguir leyendo hasta que aparece \r\n
			//TODO esto esta mal deberiamos usar el setHeaders, habria que primero hacer el if
			// y despues hacer el parser.
			String line = ParserUtils.readLine(buf, message);
			
			if(message.isNoHost() && message.isNoContentLength() && !message.isHeaderFinished()){
				// Si no esta seteado el host, y todavia no termino de parsearHeaders busco el host
				ParserUtils.parseHeaders(line, message);
			}
			
			if(message.isFinished()){
				message.state = DONE;
				return message.state.process(buf, message);
			}
			return this;
		}
		
	},
	INVALIDMETHOD {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			// hay que seguir leyendo hasta que aparece \r\n
			ParserUtils.readLine(buf, message);
			
			return this;
		}
		
	};
	
	public abstract StateHttp process(final ByteBuffer buf, final HttpMessage message);
	
}
