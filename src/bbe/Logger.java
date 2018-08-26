package bbe;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;  

public class Logger
{
	private static String pathToFile = null;

	public Logger()
	{
		
	}
	
	public Logger(String pathToFile)
	{
		this.pathToFile = pathToFile;
		
	}
	
	public static void logInfo(String message)
	{
		log("[INFO]", message);
	}
	
	public static void logWarning(String message)
	{
		log("[WARN]", message);		
	}

	public static void logError(String message)
	{
		log("[ERROR]", message);		
	}
	
	private static void log(String messageType, String message)
	{
		String fullMessage = LocalDateTime.now().toLocalTime() + " " + messageType +  " " + message;
	    System.out.println(fullMessage);
	    /*
	    if (pathToFile != null) {
	    	try {
	    		Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.pathToFile), "utf-8"));
	    		writer.write(fullMessage);
	    	}
	    	catch (IOException ex) {
	    	    System.out.println(LocalDateTime.now() + " Cannot open file '" + this.pathToFile + "'.");
	    	} 	  
	    }*/
	}
}
