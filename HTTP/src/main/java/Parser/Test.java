package Parser;

import java.nio.charset.Charset;

public class Test {

	public static void main(String[] args) {
			HTTPParser httpParser = new HTTPParser();
			ParserResponse resp;
						
			String request = "GeT / HTTP/1.1\n"+"Host: www.google.com\n\n";
    		byte[] buf = request.getBytes(Charset.forName("UTF-8"));
			
			resp = httpParser.sendData(buf);
			
			System.out.println("Host: "+resp.getHost());
			System.out.println("DoneReading: "+resp.isDoneReading());
			System.out.println("Return to client: "+resp.returnToClient());
	}

}
