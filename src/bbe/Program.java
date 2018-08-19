package bbe;

import java.io.IOException;

public class Program 
{

	// TODO remove hardcoded paths everywhere and allow args
	public static void main(String[] args) 
	{
		//if (args.length != 3)
		//	logAndExit("usage: bbe src.java dest.java");
		
		
		// TODO check recursively if the trees are isomorphic via GumTree API
		// TODO if they are not, list the changes and exit
		
		
		// if they are, proceed with the JDT API AST traversal using our custom traverser
		
		ParallelASTTraverser traverser = null;
		try {
			traverser = new ParallelASTTraverser(/*args[1]*/ "tests/test1.java", /*args[2]*/ "tests/test2.java");
		} catch (IOException e) {
			logAndExit("failed to load the source files");
		}
		
		traverser.traverse();
	}
	
	
	private static void logAndExit(String msg)
	{
		System.err.println("error: " + msg);
		System.exit(1);
	}

}
