package bbe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.gumtreediff.utils.Pair;

public class Program 
{

	// TODO remove hardcoded paths everywhere and allow args
	public static void main(String[] args) 
	{
		//if (args.length != 3)
		//	logAndExit("usage: bbe src.java dest.java");
		
		
		MappingFactory mf = null;
		try {
			mf = new MappingFactory(/*args[1]*/ "tests/test3.java", /*args[2]*/ "tests/test3dest.java");
		} catch (IOException e1) {
			logErrorAndExit("failed to create mapping");
		}
		
		if (mf.hasOnlyUpdateActions()) {
			System.out.println("The two given sources are semantically equivallent.");
			System.exit(0);
		}
		
		
		// if they are, proceed with the JDT API AST traversal using our custom traverser
		
		ASTTraverser traverser = null;
		try {
			traverser = new ASTTraverser(/*args[1]*/ "tests/test3.java", /*args[2]*/ "tests/test3dest.java", mf.getUpdates());
		} catch (IOException e) {
			logErrorAndExit("failed to load the source files");
		}
		
		HashMap<Integer, BlockVariableMap> vars = traverser.traverseSrcTree();
		
		Iterator it = vars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        System.out.println("Block: " + pair.getKey());
	        Iterator iit = ((HashMap<String, Integer>)pair.getValue()).entrySet().iterator();
	        while (iit.hasNext()) {
	            Map.Entry ipair = (Map.Entry)iit.next();
	            System.out.println(ipair.getKey() + " = " + ipair.getValue());
	        }
	    }
		
		/* bool success = */ traverser.traverseDestTree(vars);
	}
	
	
	private static void logErrorAndExit(String msg)
	{
		System.err.println("error: " + msg);
		System.exit(1);
	}

}
