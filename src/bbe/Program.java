package bbe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Program 
{	
	public static void main(String[] args) 
	{
		if (args.length < 2) {
			System.err.println("usage :./program path/to/src path/to/dest [-l (logging)] [-f (file logging)]");
			System.exit(1);
		}

		String sourceFile = args[0];
		String destFile = args[1];
		
		for (int i = 2; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				switch (args[i].substring(1).toLowerCase()) {
				case "l":
					Logger.enabled = true;
					break;
				case "f":
					Logger.pathToFile = "log.txt";
				}
			}
		}
		
		System.out.println("Analyzing:\n\tSource:\t" + sourceFile + "\n\tDest:\t" + destFile);
		
		
		MappingFactory mf = null;
		try {
			mf = new MappingFactory(sourceFile, destFile);
		} catch (IOException e1) {
			System.err.println("File(s) not found. Please check the paths.");
			Logger.logErrorAndExit("FATAL ERROR: IOException");
		}
		
		if (mf.hasOnlyVariableUpdateActions()) {
		    System.out.println("\n--- Done! ---");
			Logger.logInfo("Exiting...");
			System.exit(0);
		}
		
		
		ASTTraverser traverser = null;
		try {
			traverser = new ASTTraverser(sourceFile, destFile, mf.getUpdates());
		} catch (IOException e) {
			Logger.logErrorAndExit("Failed to load the source files");
		}

		Logger.logInfo("--- Traversing source tree... ---");
		
		HashMap<Integer, BlockVariableMap> srcVars = traverser.traverseSrcTree();

	    System.out.println("\n--- Source file variable map ---");
	    Iterator<Map.Entry<Integer, BlockVariableMap>> it = srcVars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, BlockVariableMap> pair = (Map.Entry<Integer, BlockVariableMap>)it.next();
	        System.out.println("Block id: " + pair.getKey());
	        pair.getValue().printMap();
	    }

		Logger.logInfo("--- Traversing dest tree and listing conflicts... ---");
		
		HashMap<Integer, BlockVariableMap> destVars = traverser.traverseDestTree(srcVars);	

	    System.out.println("\n--- Dest file variable map ---");
		it = destVars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, BlockVariableMap> pair = (Map.Entry<Integer, BlockVariableMap>)it.next();
	        System.out.println("Block id: " + pair.getKey());
	        pair.getValue().printMap();
	    }
		
	    System.out.println("\n--- Done! ---");
		Logger.logInfo("Exiting...");
		
		Logger.closeWriter();
	}
	

}
