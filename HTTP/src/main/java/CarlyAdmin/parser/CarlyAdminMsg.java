package CarlyAdmin.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import CarlyAdmin.manager.ConfigurationManager;
import CarlyAdmin.manager.StatisticsManager;

public class CarlyAdminMsg {
	
	private static final int BUFFER = 1024;
	
	private StatisticsManager statManager;
	private ConfigurationManager configManager;
	public ByteBuffer buffer = ByteBuffer.allocate(BUFFER);
	public CarlyAdminProtocol state = CarlyAdminProtocol.INIT;
	
	// Aca guardas los header para que no se repitan
	private List<String> statHeader;
	// Aca guardo las respuestas en el orden que corresponden
	private List<String> carlyAdminAnswer;
	
	protected CommandType type;
	protected StatusCode statuscode = null;
	private String user;

	public int lineBufferIndex;
	public byte[] lineBuffer;
	
	public CarlyAdminMsg(StatisticsManager statManager,
			ConfigurationManager configManager) {
		this.statManager = statManager;
		this.configManager = configManager;
		this.lineBuffer = new byte[BUFFER];
		this.lineBufferIndex = 0;
		statHeader = new ArrayList<String>(); 
		carlyAdminAnswer = new ArrayList<String>(); 
	}

	public void setType(CommandType type) {
		this.type = type;	
	}

	public void setUser(String user) {
		this.user = user;	
	}

	public String getUser() {
		return user;
	}

	public boolean isInvalidStatHeader(String header) {
		return (statHeader.contains(header) || 
				(!statHeader.contains(header) && !addStatHeader(header)));
	}

	private boolean addStatHeader(String header) {
		String aux = header.substring(0, header.length() - 1);
		if(aux.equalsIgnoreCase("bytes:")){
			carlyAdminAnswer.add(statManager.printBytes());
		} else if(aux.equalsIgnoreCase("status-code:")){
			carlyAdminAnswer.add(statManager.printStatusCode());
		} else if(aux.equalsIgnoreCase("number-of-access:")){
			carlyAdminAnswer.add(statManager.printAccess());
		} else {
			return false;
		}
		return true;
	}

	public boolean isInvalidConfigHeader(String header) {
		if(isL33t(header) || isUserConfig(header)){
			return false;
		}
		return true;
	}

	private boolean isL33t(String header) {
		if(header.equalsIgnoreCase("l33t:\n")){
			carlyAdminAnswer.add(configManager.printL33t());
		}else if(header.equalsIgnoreCase("l33t: on\n")){
			configManager.setL33t(true);
		}else if(header.equalsIgnoreCase("l33t: off\n")){
			configManager.setL33t(false);
		}else{
			return false;
		}
		return true;
	}

	private boolean isUserConfig(String header) {
		if(header.startsWith("change-user: ")){
			int sizeHeader = 13;
			String user = header.substring(sizeHeader, header.length() - 1);
			if(!configManager.changeUser(user)){
				return false;
			}
		}else if(header.startsWith("change-pass: ")){
			int sizeHeader = 17;
			String pass = header.substring(sizeHeader, header.length() - 1);
			if(!configManager.changePass(pass)){
				return false;
			}
		}else{
			return false;
		}
		return true;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public boolean isFinished() {	
		if(state.equals(CarlyAdminProtocol.CLOSE)){
			return true;
		}
		return false;
	}

	public CarlyAdminMsg handleRead() {
		state = state.handleRead(this);
		return this;
	}

	public String getResponse() {
		String aux = "Code: " + statuscode.getCode() + "\n";
		aux += "Code-Detail: " + statuscode.getDetail() + "\n";
		
		if(statuscode.equals(StatusCode.SUCCESS)){
			for(String ans: carlyAdminAnswer){
				aux += ans + "\n";
			}
		}
		return aux;
	}


}
