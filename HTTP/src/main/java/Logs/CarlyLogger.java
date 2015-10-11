package Logs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class CarlyLogger {
	
	private static Date fecha = new Date();
    private Logger log;
    private String workspace = "../Carly/";
    private static CarlyLogger carlyLogger = null;
    
    public CarlyLogger(String nameClass) throws IOException{
		
    	log = Logger.getLogger(Logger.class);
		// Formato de la hora
		fecha = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("dd.MM.yyyy");
        String fechaAc = formato.format(fecha);
        // Patrón que seguirá las lineas del log
        PatternLayout defaultLayout = new PatternLayout("%p: %d{HH:mm:ss} --> %m%n");
        RollingFileAppender rollingFileAppender = new RollingFileAppender();
        //Definimos el archivo dónde irá el log (la ruta)
        //cambiar la ruta, podria ser C:/tmp/
        rollingFileAppender.setFile(workspace + "logs/"+nameClass+"log_" + fechaAc + ".log", true, false, 0);
        rollingFileAppender.setLayout(defaultLayout);
        
        log.removeAllAppenders();
        log.addAppender(rollingFileAppender);
        log.setAdditivity(false);
	}
    
    public Logger getLog() {
		return log;
	}
    public void setLog(Logger log) {
        this.log = log;
    }
    
    public static Logger getCarlyLogger(){
    	Date fechaActual = new Date();
    	
    	if(carlyLogger == null || fechaActual.after(fecha)){
    		try{
    			carlyLogger = new CarlyLogger("CarlyLogger");
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error al crear el logger");
				e.printStackTrace();			
			}
    	}
    	
    	return carlyLogger.getLog();
    }
    

}
