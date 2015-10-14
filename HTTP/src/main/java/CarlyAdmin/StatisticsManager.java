package CarlyAdmin;

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

}
