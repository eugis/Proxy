package CarlyAdmin.manager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import Logs.CarlyLogger;

public class StatisticsManager {
	
	private AtomicInteger bytes;
	private ConcurrentHashMap<String, Integer> statusCodes;
	private AtomicInteger requestAccess;
	
	private static StatisticsManager INSTANCE = null;
	private Logger logs = CarlyLogger.getCarlyLogger();
	
	private StatisticsManager() {
		this.bytes = new AtomicInteger(0);
		this.statusCodes = new ConcurrentHashMap<String, Integer>();
		this.requestAccess = new AtomicInteger(0);
	}
	
	public static StatisticsManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new StatisticsManager();
		return INSTANCE;
	}
	
	public void addBytes(int bytes){
		this.bytes.addAndGet(bytes);
	}
	
	public int getBytes() {
		return bytes.get();
	}

	public String printBytes() {
		String bytes = "Bytes: " + getBytes();
		logs.info("StatisticsManager: printBytes - " + bytes);
		return bytes;
	}
	
	public void addStatusCode(String statusCode){
		if(statusCodes.get(statusCode) == null){
			statusCodes.put(statusCode, 0);
		}
		statusCodes.put(statusCode, statusCodes.get(statusCode) + 1);
	}

	public String printStatusCode() {
		String resp = "Status-Codes:";
		if(statusCodes.size() > 0){
			Set<String> keys = statusCodes.keySet();
			for(String code: keys){
				int cant = statusCodes.get(code);
				resp = resp + "{" + code + ","  + cant + "}";
			}
		} else{
			resp = resp + "no hay statusCode";
		}
		logs.info("StatisticsManager: printStatusCode - " + resp);
		return resp;
	}
	
	public void incRequestAccess(){
		this.requestAccess.addAndGet(1);
	}
	
	public int getRequestAccess() {
		return requestAccess.get();
	}
	
	public String printAccess() {
		String access = "Number-of-Access: " + getRequestAccess();
		logs.info("StatisticsManager: printAccess - " + access);
		return access;
	}


}
