package bbe;

import java.io.IOException;
import java.util.ArrayList;

import com.github.gumtreediff.utils.Pair;

public class Program 
{

	// TODO remove hardcoded paths everywhere and allow args
	public static void main(String[] args) 
	{
		//if (args.length != 3)
		//	logAndExit("usage: bbe src.java dest.java");
		
		
		// TODO check recursively if the trees are isomorphic via GumTree API
		// TODO if they are not, list the changes and exit
		MappingFactory mf = null;
		try {
			mf = new MappingFactory(/*args[1]*/ "tests/test1.java", /*args[2]*/ "tests/test2.java");
		} catch (IOException e1) {
			logErrorAndExit("failed to create mapping");
		}
		
		if (mf.hasOnlyUpdateActions()) {
			System.out.println("The two given sources are semantically equivallent.");
			System.exit(0);
		}
		
		
		// if they are, proceed with the JDT API AST traversal using our custom traverser
		
		ParallelASTTraverser traverser = null;
		try {
			traverser = new ParallelASTTraverser(/*args[1]*/ "tests/test1.java", /*args[2]*/ "tests/test2.java", mf.getUpdates());
		} catch (IOException e) {
			logErrorAndExit("failed to load the source files");
		}
		
		traverser.traverse();
	}
	
	
	private static void logErrorAndExit(String msg)
	{
		System.err.println("error: " + msg);
		System.exit(1);
	}

}
