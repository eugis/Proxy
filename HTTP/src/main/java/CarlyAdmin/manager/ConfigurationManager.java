package CarlyAdmin.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
		this.authorization = new ConcurrentHashMap<String, String>();
		this.l33t = new AtomicBoolean(false);
		InputStream is = getClass().getResourceAsStream(
				"../resources/setup.properties");
		Properties p = new Properties();
		
		try {
			p.load(is);
			String user = p.getProperty("carlyAdmin-user");
			authorization.put("user", user);
			String pass = p.getProperty("carlyAdmin-pass");
			authorization.put("pass", pass);
			
		} catch (IOException e) {
			logs.error("ConfigurationManager - Missing configuration file", e);
			throw new RuntimeException("Missing configuration file...");
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logs.error("ConfigurationManager - Error occured when reading the configuration file", e);
				throw new RuntimeException("Error when reading the configuration file");
			}
		}
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
		String aux = user.trim();
		if(aux == null || aux.equals("")){
			return false;
		}
		authorization.put("user", user);
		return true;
	}

	public boolean changePass(String pass) {
		String aux = pass.trim();
		if(aux == null || aux.equals("")){
			return false;
		}
		authorization.put("pass", pass);
		return true;
	}



}
