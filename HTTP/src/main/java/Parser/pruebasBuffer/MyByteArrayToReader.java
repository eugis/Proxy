package Parser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 
public class MyByteArrayToReader {
 
    public static void main(String a[]){
         
//        String str = "converting to input stream"+
//                        "\n\n and this is second line";
    	
    	String str = "convirtiendo\n";
    	    	
        byte[] content = str.getBytes();
        InputStream is = null;
        BufferedReader bfReader = null;
        
        
        try {
            is = new ByteArrayInputStream(content);
            bfReader = new BufferedReader(new InputStreamReader(is));
            String temp = null;
            
            int i =0;
            while((temp = readLine(bfReader)) != null){
                System.out.println(temp +" "+i);
                i++;
            }
        } finally {
            try{
                if(is != null) is.close();
            } catch (Exception ex){
                 
            }
        }
         
    }
    
    private static String readLine(final BufferedReader buf){
    	String ret=null;
    	try {
			ret= buf.readLine();
			if(ret!=null && ret.trim().isEmpty()){
				System.out.print("----empty line----");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return ret;
    }
}
// See more at: http://java2novice.com/java-file-io-operations/byte-array-to-reader/#sthash.a8wX95zO.dpuf