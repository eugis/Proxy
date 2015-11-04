package Parser;

import java.util.HashSet;
import java.util.Set;

import CarlyAdmin.manager.ConfigurationManager;

public class ParserUtils {
	
	private static final Set<String> validMethods = loadMethods();
	
	public static boolean isLeetEnabled(){
		return ConfigurationManager.getInstance().isL33t();
	}
	
	private static Set<String> loadMethods() {
		Set<String> headers = new HashSet<String>();
		headers.add("GET");
		headers.add("POST");
		headers.add("HEAD");
		return headers;
	}

	public static String toLeet(String text){
		if(isLeetEnabled()){
			text = text.replace('a', '4');
			text = text.replace('e', '3');
			text = text.replace('i', '1');
			text = text.replace('o', '0');
			text = text.replace('c', '<');
		}
		return text;
	}
	
	public boolean isValidMethod(String method){
		return validMethods.contains(method);
	}


}
