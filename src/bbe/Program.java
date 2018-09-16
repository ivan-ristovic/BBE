package bbe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Program 
{	
	// TODO remove hardcoded paths everywhere and allow args
	public static void main(String[] args) 
	{
		// Logger.pathToFile = "log.txt";

		String sourceFile = null;
		String destFile = null;
		
		if (args.length < 2) {
			// TODO remove
			Logger.logInfo("Using default test files.");
			sourceFile = "tests/nested_class.java";
			destFile = "tests/nested_class_equivallent.java";
		} else {
			sourceFile = args[0];
			destFile = args[1];
		}
		Logger.logInfo("Analyzing:\n\tSource:\t" + sourceFile + "\n\tDest:\t" + destFile);
		
		
		MappingFactory mf = null;
		try {
			mf = new MappingFactory(sourceFile, destFile);
		} catch (IOException e1) {
			Logger.logErrorAndExit("One of the given filenames does not point to a valid file.");
		}
		
		if (mf.hasOnlyVariableUpdateActions()) {
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

		// TODO remove or beautify if we wish to show end results
		Iterator<Map.Entry<Integer, BlockVariableMap>> it = srcVars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, BlockVariableMap> pair = (Map.Entry<Integer, BlockVariableMap>)it.next();
	        System.out.println("Block id: " + pair.getKey());
	        pair.getValue().printMap();
	    }

		Logger.logInfo("--- Traversing dest tree and listing conflicts... ---");
		HashMap<Integer, BlockVariableMap> destVars = traverser.traverseDestTree(srcVars);	

		// TODO remove or beautify if we wish to show end results
		it = destVars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, BlockVariableMap> pair = (Map.Entry<Integer, BlockVariableMap>)it.next();
	        System.out.println("Block id: " + pair.getKey());
	        pair.getValue().printMap();
	    }
		
		Logger.logInfo("--- Done! ---");
		
		Logger.closeWriter();
	}
	

}
