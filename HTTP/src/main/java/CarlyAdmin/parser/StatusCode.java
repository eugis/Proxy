package CarlyAdmin.parser;

public enum StatusCode {
	
	SUCCESS(10, "Ok"), ERROR_ACTION(50, "Error en el pedido"), 
	AUTHORIZATION_FAILED(30, "Usuario o Contraseña invalida"), 
	HEADER_ERROR(51, "Header invalido");
	
	private int code;
	private String detail;
	
	private StatusCode(int code, String detail) {
		this.code = code;
		this.detail = detail;
	}

	public int getCode() {
		return code;
	}
	
	public String getDetail() {
		return detail;
	}

}
