package bbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.*;

import com.github.gumtreediff.utils.Pair;


@SuppressWarnings("unchecked")
public class DestASTVisitor extends ASTVisitor
{
	private HashMap<Integer, BlockVariableMap> expectedVars;
	private ArrayList<Block> conflictingBlocks;
	private ArrayList<String> conflictingVars;

	
	public DestASTVisitor(HashMap<Integer, BlockVariableMap> expectedVars, ArrayList<Pair<String, String>> renames)
	{
		this.expectedVars = expectedVars;
		Iterator<Entry<Integer, BlockVariableMap>> it = this.expectedVars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, BlockVariableMap> pair = (Map.Entry<Integer, BlockVariableMap>)it.next();
	        // pair.getValue().renameVars(renames);
	    }
	}
	

	public ArrayList<Block> getConflictingBlocks()
	{
		return this.conflictingBlocks;
	}

	public ArrayList<String> getConflictingVars()
	{
		return this.conflictingVars;
	}
}
