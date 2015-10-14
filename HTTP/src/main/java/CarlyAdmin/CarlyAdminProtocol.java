package CarlyAdmin;

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
			// TODO Auto-generated method stub
			return null;
		}
		
	},
	HEADERCONFIG{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			// TODO Auto-generated method stub
			return null;
		}
		
	},
	HEADERSTAT{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			// TODO Auto-generated method stub
			return null;
		}
		
	},
	END{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			// TODO Auto-generated method stub
			return null;
		}
		
	},
	CLOSE{

		@Override
		protected CarlyAdminProtocol handleRead(CarlyAdminMsg msg) {
			// TODO Auto-generated method stub
			return null;
		}
		
	};

	protected abstract CarlyAdminProtocol handleRead(CarlyAdminMsg msg);
	
	private static String parse(CarlyAdminMsg msg) {
		// TODO Auto-generated method stub
		return null;
	}
}
