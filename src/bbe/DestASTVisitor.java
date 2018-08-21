package bbe;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;

import com.github.gumtreediff.utils.Pair;


@SuppressWarnings("unchecked")
public class DestASTVisitor extends ASTVisitor
{
	private HashMap<Integer, HashMap<String, Integer>> expectedVars;
	private boolean conflictFound;
	private String errorMessage;

	
	public DestASTVisitor(HashMap<Integer, HashMap<String, Integer>> expectedVars, ArrayList<Pair<String, String>> renames)
	{
		this.expectedVars = expectedVars;
		for (Pair<String, String> pair : renames) {
			// TODO rename vars in this.expectedVars
		}
		this.conflictFound = false;
	}
	
	
	public boolean isConflictFound()
	{
		return conflictFound;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
