package CarlyAdmin.parser;

import org.apache.log4j.Logger;

import CarlyAdmin.manager.ConfigurationManager;
import Logs.CarlyLogger;


public enum CarlyAdminProtocol {
	
	INIT{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			CarlyAdminProtocol ret = this;
			String line = parse(msg);
			if(line != null){
				if(line.equalsIgnoreCase("CARLYADMIN Stat\n")){
					msg.setType(CommandType.STAT);
					ret = USER;
				}else if(line.equalsIgnoreCase("CARLYADMIN Config\n")){
					msg.setType(CommandType.CONFIG);
					ret = USER;
				}else{
					ret = CLOSE;
					msg.statuscode = StatusCode.ERROR_ACTION;
				}
			}
			return ret;
		}
		
	},
	USER{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			CarlyAdminProtocol ret = this;
			String line = parse(msg);
			boolean userOk = false;
			if(line != null){
				if(line.startsWith("user: ")){
					String[] user = line.split(" ");
					if(user.length == 2){
						msg.setUser(user[1].trim());
						ret = PASS;
						userOk = true;
					}
				}
				if(!userOk){
					msg.statuscode = StatusCode.ERROR_ACTION;
					ret = CLOSE;
				}
			}
			return ret;
		}
		
	},
	PASS{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			CarlyAdminProtocol ret = this;
			String line = parse(msg);
			boolean passOk = false;
			
			if(line != null){
				if(line.startsWith("pass: ")){
					String[] pass = line.split(" ");
					if(pass.length == 2){
						if(ConfigurationManager.getInstance().authorized(
								msg.getUser(), pass[1].trim())){
							if(msg.type.equals(CommandType.CONFIG)){
								ret = CONFIG;
							} else {
								ret = STAT;
							}
							passOk = true;
						}else{
							msg.statuscode = StatusCode.AUTHORIZATION_FAILED;
							ret = CLOSE;
							passOk = true;
						}
					}
				}
				if(!passOk){
					msg.statuscode = StatusCode.ERROR_ACTION;
					ret = CLOSE;
				}
			}
			
			return ret;
		}
		
	},
	CONFIG{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			CarlyAdminProtocol ret = this;
			String header = parseHeaders(msg);
			if(header != null){
				if(msg.isInvalidConfigHeader(header)){
					msg.statuscode = StatusCode.HEADER_ERROR;
					ret = CLOSE;
				}
			} else {
				ret = END;
			}
			return ret;
		}
		
	},
	STAT{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			CarlyAdminProtocol ret = this;
			String header = parseHeaders(msg);
			if(header != null){
				if(msg.isInvalidStatHeader(header)){
					msg.statuscode = StatusCode.HEADER_ERROR;
					ret = CLOSE;
				}
			}else{
				ret = END;
			}
			
			return ret;
		}
		
	},
	END{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			CarlyAdminProtocol ret = this;
			String header = parseHeaders(msg);
			boolean endOk = false;
			if(header != null){
				if(header.equalsIgnoreCase("END\n")){
					msg.statuscode = StatusCode.SUCCESS;
					ret = CLOSE;
					endOk = true;
				}
			}
			if(!endOk){
				msg.statuscode = StatusCode.ERROR_ACTION;
				ret = CLOSE;
			}
			
			return ret;
		}
		
	},
	CLOSE{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			return null;
		}
		
	};

	protected abstract CarlyAdminProtocol handleRead(CarlyAdminMsg msg);
	private static Logger logs = CarlyLogger.getCarlyLogger();
	
	private static String parse(CarlyAdminMsg msg) {
		msg.buffer.flip();
		
		while(msg.buffer.hasRemaining()){
			byte c = msg.buffer.get();
			msg.lineBuffer[msg.lineBufferIndex++] = c;
			if(msg.lineBufferIndex == msg.lineBuffer.length){
				logs.error("State Protocol - parse: linea muy larga");
				throw new RuntimeException("linea muy larga");
			}
			if(c == '\n'){
				String line = new String(msg.lineBuffer, 0, 
						msg.lineBufferIndex);
				msg.lineBufferIndex = 0;
				msg.buffer.compact();
				return line.toLowerCase();
			} 
		}
		msg.buffer.compact();
		return null;
	}
	
	private static String parseHeaders(CarlyAdminMsg msg) {
		String ans = null;
		msg.buffer.flip();
		
		while(msg.buffer.hasRemaining()){
			byte c = msg.buffer.get();
			msg.lineBuffer[msg.lineBufferIndex++] = c;
			if(msg.lineBufferIndex == msg.lineBuffer.length){
				logs.error("StateProtocol - parseHeaders: linea muy larga");
				throw new RuntimeException("linea muy larga");
			}
			if(c == '\n'){
				if(msg.lineBufferIndex != 1){
					ans = new String(msg.lineBuffer, 0,
							msg.lineBufferIndex);
				} else {
					ans = null;
				}
			}
		}
		msg.lineBufferIndex = 0;
		msg.buffer.compact();
		return ans;
	}
}
