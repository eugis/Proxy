package CarlyAdmin;

public class ConfigurationManager {
	
	private static ConfigurationManager INSTANCE = null;
	
	public static ConfigurationManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ConfigurationManager();
		}
		return INSTANCE;
	}

}
