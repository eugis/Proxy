package Parser2;

import java.nio.ByteBuffer;

import Parser2.ParserUtils;

public enum StateHttp {
	
	REQUEST_LINE {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			String line = ParserUtils.readLine(buf, message);
			if(line == null){
				return this;
			}
			boolean valid = ParserUtils.parseMethod(line.trim(), message);
			if(valid){
				message.state = HEADER;
				return message.state.process(buf, message);
			}
			
			return INVALID;
		}
		
	},
	HEADER {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			String line = ParserUtils.readLine(buf, message);
			if(line == null){
				return this;
			}
			boolean valid = ParserUtils.parseHeaders(line.trim(), message);
			if(!valid){
				return INVALID;
			}
			if(message.headerFinished()){
				message.state = EMPTY_LINE;
				return message.state.process(buf, message);
			}
			return this;
		}
		
	},
	EMPTY_LINE {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			String line = ParserUtils.readLine(buf, message);
			if(line == null){
				return this;
			}
			if(line.equals("\n")){
				message.state = BODY;
				return message.state.process(buf, message);
			}
			return INVALID;
		}
		
	},
	BODY {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			boolean finished = ParserUtils.parseData(buf, message);
			if(finished){
				return DONE;
			}
			return this;
		}
		
	},
	DONE {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			// TODO me parece q aca no hay q hacer nada
			return this;
		}
		
	},
	INVALID {

		@Override
		public StateHttp process(ByteBuffer buf, HttpMessage message) {
			// TODO me parece q aca no hay que hacer nada
			return this;
		}
		
	};
	
	public abstract StateHttp process(final ByteBuffer buf, final HttpMessage message);
}
