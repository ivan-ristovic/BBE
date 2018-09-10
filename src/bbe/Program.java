package bbe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.gumtreediff.utils.Pair;

public class Program 
{	
	// TODO remove hardcoded paths everywhere and allow args
	public static void main(String[] args) 
	{
		//Logger.pathToFile = "log.txt";

		String sourceFile = null;
		String destFile = null;
		
		if (args.length < 2) {
			Logger.logInfo("Using default test files.");
			sourceFile = "tests/test3.java";
			destFile = "tests/test3dest.java";
		} else {
			sourceFile = args[0];
			destFile = args[1];
		}
		Logger.logInfo("Using test files: Source: " + sourceFile + ", Dest: " + destFile);
		
		
		MappingFactory mf = null;
		try {
			mf = new MappingFactory(sourceFile, destFile);
		} catch (IOException e1) {
			Logger.logErrorAndExit("failed to create mapping");
		}
		
		/* FIXME
		if (mf.hasOnlyUpdateActions()) {
			Logger.logInfo("The two given sources are semantically equivallent.");
			System.exit(0);
		}
		*/
				
		// if they are, proceed with the JDT API AST traversal using our custom traverser
		
		ASTTraverser traverser = null;
		try {
			traverser = new ASTTraverser(sourceFile, destFile, mf.getUpdates());
		} catch (IOException e) {
			Logger.logErrorAndExit("Failed to load the source files");
		}

		Logger.logInfo("Traversing source tree.");
		HashMap<Integer, BlockVariableMap> vars = traverser.traverseSrcTree();
		Iterator it = vars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        System.out.println("Block: " + pair.getKey());
	        Iterator<?> iit = ((HashMap<String, Integer>)pair.getValue()).entrySet().iterator();
	        while (iit.hasNext()) {
	            Map.Entry ipair = (Map.Entry)iit.next();
	            System.out.println(ipair.getKey() + " = " + ipair.getValue());
	        }
	    }

		// Logger.logInfo("Traversing dest tree.");
		// traverser.traverseDestTree(vars);
		
		
		Logger.closeWriter();
	}
	

}
