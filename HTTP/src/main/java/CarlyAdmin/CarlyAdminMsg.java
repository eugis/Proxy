package CarlyAdmin;

public class CarlyAdminMsg {
	
	private StatisticsManager statManager;
	private ConfigurationManager configManager;
	
	private CommandType type;
	protected StatusCode statuscode = null;
	private String user;
	
	public CarlyAdminMsg(StatisticsManager statManager,
			ConfigurationManager configManager) {
		this.statManager = statManager;
		this.configManager = configManager;
	}

	public void setType(CommandType type) {
		this.type = type;	
	}

	public void setUser(String user) {
		this.user = user;	
	}

}
