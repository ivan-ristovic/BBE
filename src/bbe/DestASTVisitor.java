package bbe;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;


@SuppressWarnings("unchecked")
public class DestASTVisitor extends ASTVisitor
{
	private HashMap<Integer, HashMap<String, Integer>> expectedVars;
	private boolean conflictFound;
	private String errorMessage;

	
	public DestASTVisitor(HashMap<Integer, HashMap<String, Integer>> expectedVars)
	{
		this.expectedVars = expectedVars;
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
