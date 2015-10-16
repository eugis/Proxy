package CarlyAdmin.parser;

import CarlyAdmin.manager.ConfigurationManager;


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
	
	private static String parse(CarlyAdminMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String parseHeaders(CarlyAdminMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}
}
