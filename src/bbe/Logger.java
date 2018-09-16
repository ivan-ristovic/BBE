package bbe;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;  

public class Logger
{
	public static String pathToFile = null;
	public static boolean enabled = false;
	private static Writer writer = null;

	public Logger()
	{
		
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
	
	public static void logErrorAndExit(String message)
	{
		logError(message);
		System.exit(1);
	}
	
	private static void log(String messageType, String message)
	{
		if (!enabled)
			return;
		
		String fullMessage = LocalDateTime.now().toLocalTime() + " " + messageType +  " " + message;
	    System.err.println(fullMessage);
	    if (pathToFile != null) {
	    	try {
	    		if (writer == null)
	    			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathToFile)));
		    	writer.append(fullMessage + "\n");
	    		writer.flush();
	    	}
	    	catch (IOException ex) {
	    	    System.out.println("[ERROR] " + LocalDateTime.now() + " Cannot open file '" + pathToFile + "'.");
	    	}
	    }
	}

	public static boolean closeWriter()
	{
		if (writer != null)
			try {
				logInfo("Closing writer to file '" + pathToFile + "'");
				writer.close();
			} catch (IOException e) {
				return false;
			}
		return true;
	}
	
}
