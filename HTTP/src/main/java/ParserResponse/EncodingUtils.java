package ParserResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class EncodingUtils {
	
	public static byte[] decompressGzip(byte[] buffer){
		byte[] aux = new byte[buffer.length];
		
		InputStream in = new ByteArrayInputStream(buffer);
		try {
			GZIPInputStream gzip = new GZIPInputStream(in);
			gzip.read(aux);
			gzip.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return aux;
	}
	
	public static byte[] compressGzip(byte[] content){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzip=new GZIPOutputStream(baos);
			gzip.write(content);
			gzip.close();
		}
		catch (  IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
//	public static void main(String[] args) {
//		String prueba = "hola";
//		
//		byte[] compress = EncodingUtils.decompressGzip(prueba.getBytes());
//		System.out.println(new String(compress));
//		
//		byte[] decode = EncodingUtils.decodeGzip(compress);
//		System.out.println(new String(decode));
//	}
	

}
