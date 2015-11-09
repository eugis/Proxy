package CarlyAdmin.parser;


public class CarlyResponse {
	
	private CarlyAdminMsg msg;

	public CarlyResponse(CarlyAdminMsg msg) {
		this.msg = msg;
	}

	public String getCarlyAns() {
		String response = "CARLYADMIN Answer\n";
		
		response += msg.getResponse();
		
		// linea vacia
		response = response + "\n";
		response = response + "END \n";
		
		return response;
	}

}
