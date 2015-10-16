package CarlyAdmin.manager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;

public class ConfigurationManager {
	
	private static ConfigurationManager INSTANCE = null;
	private ConcurrentHashMap<String, String> authorization;
	
	private AtomicBoolean l33t;
	
	private Logger logs = CarlyLogger.getCarlyLogger();
	
	public ConfigurationManager() {
		// TODO Auto-generated constructor stub
	}
	
	public static ConfigurationManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ConfigurationManager();
		}
		return INSTANCE;
	}

	public boolean authorized(String user, String pass) {
		if(!authorization.containsKey("user") ||
				!authorization.containsKey("pass")){
			return false;
		}
		if(!authorization.get("user").equalsIgnoreCase(user))
			return false;
		if(!authorization.get("pass").equalsIgnoreCase(pass))
			return false;
		return true;
	}
	
	public void setL33t(boolean b) {
		l33t.set(b);	
	}
	
	public boolean isL33t() {
		return l33t.get();
	}

	public String printL33t() {
		return "l33t: " + isL33t();
	}

	public boolean changeUser(String user) {
		// TODO Auto-generated method stub
		String aux = user.trim();
		if(aux == null || aux.equals("")){
			return false;
		}
		authorization.put("user", user);
		return true;
	}



}
