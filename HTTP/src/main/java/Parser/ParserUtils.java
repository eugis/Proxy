package Parser;

import CarlyAdmin.manager.ConfigurationManager;

public class ParserUtils {
	
	public static boolean isLeetEnabled(){
		return ConfigurationManager.getInstance().isL33t();
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

}
