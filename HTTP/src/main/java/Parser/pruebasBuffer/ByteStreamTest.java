package Parser;
import java.io.*;


public class ByteStreamTest {

	public static void main(String[] args) {
		 ByteArrayOutputStream bOutput = new ByteArrayOutputStream(12);

	      while( bOutput.size()!= 10 ) {
	         // Gets the inputs from the user
	         try {
				bOutput.write(System.in.read());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
	      }

	      byte b [] = bOutput.toByteArray();
	      System.out.println("Print the content");
	      for(int x= 0 ; x < b.length; x++) {
	         // printing the characters
	         System.out.print((char)b[x]  + "   "); 
	      }
	      System.out.println("   ");

	      int c;

	      ByteArrayInputStream bInput = new ByteArrayInputStream(b);

	      System.out.println("Converting characters to Upper case " );
	      for(int y = 0 ; y < 1; y++ ) {
	         while(( c= bInput.read())!= -1) {
	            System.out.println(Character.toUpperCase((char)c));
	         }
	         bInput.reset(); 
	      }

	}

}
