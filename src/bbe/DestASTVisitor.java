package bbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.grammar.v3.ANTLRParser.label_return;
import org.eclipse.jdt.core.dom.*;

import com.github.gumtreediff.utils.Pair;


@SuppressWarnings("unchecked")
public class DestASTVisitor extends ASTVisitor
{
	private HashMap<Integer, BlockVariableMap> expectedVars;
	private HashMap<Integer, BlockVariableMap> blockVars;
	private ArrayList<Pair<String, String>> updates;
	private String errorMessage;
	private boolean fatalError;

	
	public DestASTVisitor(HashMap<Integer, BlockVariableMap> expectedVars, ArrayList<Pair<String, String>> renames)
	{
		this.blockVars = new HashMap<Integer, BlockVariableMap>();
		this.blockVars.put(0, new BlockVariableMap(renames));
		this.expectedVars = expectedVars;
		this.updates = renames;
		/*Iterator<Entry<Integer, BlockVariableMap>> it = this.expectedVars.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Integer, BlockVariableMap> pair = (Map.Entry<Integer, BlockVariableMap>)it.next();
	        pair.getValue().renameVars(renames);
	    }*/
	}
	
	
	public HashMap<Integer, BlockVariableMap> getDeclaredVars() 
	{
		return this.blockVars;
	}

	public boolean visit(Block node)
	{
		int parentDepth = ASTNodeUtils.getBlockDepth(node.getParent());
		if (fatalError && !this.expectedVars.containsKey(parentDepth)) {
			errorMessage = "block missmatch (" + parentDepth + ")";
			return false;
		}
		this.blockVars.put(parentDepth + 1, new BlockVariableMap(this.blockVars.get(parentDepth), updates, node));
		return true;
	}

	public void endVisit(Block node) 
	{	
		int parentDepth = ASTNodeUtils.getBlockDepth(node.getParent());
		int currentDepth = parentDepth + 1;
		Iterator<Entry<String, Integer>> it = this.blockVars.get(currentDepth).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
	        this.blockVars.get(parentDepth).computeIfPresent(pair.getKey(), (k, v) -> pair.getValue());
	    }
	    
	    ArrayList<String> conflictingVars = new ArrayList<String>();
	    boolean hasBlockConflicts = false;
	    it = this.expectedVars.get(currentDepth).entrySet().iterator();
	    while (it.hasNext()) {		    
	        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
	    	String srcName = pair.getKey();
	    	Pair<String, String> rename = this.blockVars.get(currentDepth).getRenamePair(srcName);
	    	// If the name is not in the map, this indicates that it is the same in src and in dest.
	    	String destName = rename != null ? rename.second : srcName;
	    	if (destName == MappingFactory.MISSING) {
	    		Logger.logError("Variable (" + srcName + ") doesn't exist in dest.");
	    		// TODO should non-existing war be in conflicting vars?
	    		//conflictingVars.add(srcName);
				//hasBlockConflicts = true;
	    	}
	    	else {		    	
				int srcValue = this.expectedVars.get(currentDepth).get(srcName);
				int destValue = this.blockVars.get(currentDepth).get(destName);
				if (srcValue != destValue) {
					Logger.logError("Different value of variable: " + srcName + "(" + srcValue + ") != " + destName + "(" + destValue + ")");
					conflictingVars.add(srcName);
					hasBlockConflicts = true;
				}
	    	}
	    }
	    Logger.logInfo("Block (" + currentDepth + ") traversed with conflicts: " + hasBlockConflicts);
	    if (hasBlockConflicts)
	    {
	    	Logger.logInfo("Conflicting vars: " + conflictingVars.toString());
	    	Logger.logInfo("Deep diving into into block (" + currentDepth + ")");
		    Block sourceBlock = this.expectedVars.get(currentDepth).getCurrentBlock();
		    compareBlocks(sourceBlock, node, conflictingVars);
	    }
	}
	
	private boolean compareBlocks(Block src, Block dest, ArrayList<String> conflictingVars) 
	{
		int id = ASTNodeUtils.getBlockDepth(src);
		Logger.logInfo("Comparing blocks with id (" + id + ") with conflicting vars (" + conflictingVars + ")");

	    List<Statement> srcStatements = src.statements();
	    List<Statement> destStatements = dest.statements();
	    // Investigate further every conflicting var. Find all statements in which it appears in src,
	    // and in parallel search for the equivalent in the dest.
	    // Compare founded statements and determine the difference	    
		for (String conflictingVar : conflictingVars) {
			Iterator<Statement> it = destStatements.iterator();
			
			for (Statement srcStatement : srcStatements) {
				if (statementContainsVar(srcStatement, conflictingVar)) {
					boolean compared = false;
					while(it.hasNext()) {
						Statement destStatamenet = it.next();
						if (statementContainsVar(destStatamenet, conflictingVar)) {
							//TODO compare statements
							boolean result = compareStatements(srcStatement, destStatamenet);
							if (result == false) {
								Logger.logError("Statements are different!" + srcStatement + " " + destStatamenet);
								// TODO handle this!
							}
							compared = true;
							break;
						}
					}
					if (!compared) {
						Logger.logError("No statement in dest that src statement '" + srcStatement + "' can comare to.");
					}					

				}				
			}
			// TODO check if there are statements in dest that don't exist in src
		}
	    
		return true;
	}
	
	private boolean compareStatements(Statement src, Statement dest)
	{
		Logger.logInfo("Comparing statements: src: " + src + ",  dest: " + dest);
		
		// Has to be blockVars, because expectedVars throws nullPointer exception, don't have updates
		// We didn't make it with constructor that initializes updates
		BlockVariableMap map = this.blockVars.get(1);
		
		map.checkDeclarationStatements((VariableDeclarationStatement)src, (VariableDeclarationStatement)dest);
		
		return false;
	}
	
	private boolean statementContainsVar(Statement stmt, String var)
	{
		// TODO find a better way to do this
		int ind = stmt.toString().indexOf(var + "=");
		return ind == -1 ? false : true;
	}
	
	public boolean visit(VariableDeclarationStatement node) 
	{
		Type type = node.getType();
		for (Modifier modifier : (List<Modifier>)node.modifiers()) {
			// TODO check modifiers for each variable if they match in both files
			// if we want to support modifiers, then the var value in map has to be a class type :(
		}
		return true;
	}
	
	public boolean visit(VariableDeclarationFragment node) 
	{
		int blockHashCode = ASTNodeUtils.getBlockDepth(node);
		SimpleName name = node.getName();
		Expression expr = node.getInitializer();
		
		int value = 0;
		
		// If declaration without initialization
		if (expr == null)
			value = Integer.MAX_VALUE;
		else {
			// If right side is number ex. x = 5
			if (expr.getNodeType() == Type.NUMBER_LITERAL)
				value = Integer.parseInt(expr + "");
			// If right side is infix expression ex. x = a + b
			else if (expr.getNodeType() == Type.INFIX_EXPRESSION)
				value = visitInfix((InfixExpression)expr);
			// If right side is simple name ex. x = y
			else if (expr.getNodeType() == Type.SIMPLE_NAME) {
				// If it is variable and we have it in map
				if (this.blockVars.get(blockHashCode).containsKey(expr + ""))
					value = this.blockVars.get(blockHashCode).get(expr + "");
				else 
					value = Integer.MAX_VALUE;
			}
		}
		
		this.blockVars.get(blockHashCode).put(new String(name + ""), value);

		return false;
	}

	public boolean visit(Assignment node) 
	{
		int blockHashCode = ASTNodeUtils.getBlockDepth(node);
		String identifier = node.getLeftHandSide() + "";
		String operator = node.getOperator() + "";
		int value;
		
		// Case where we have number on the right side
		if (node.getRightHandSide().getNodeType() == Type.NUMBER_LITERAL)
			value = Integer.parseInt(node.getRightHandSide() + "");
		// Case where we have infix expression on the right side
		else if (node.getRightHandSide().getNodeType() == Type.INFIX_EXPRESSION) {
			value = visitInfix((InfixExpression)node.getRightHandSide());
		}
		//Case where we have one variable on the right side
		else {
			String rightSideIdentifier = node.getRightHandSide() + "";
			value = this.blockVars.get(blockHashCode).get(rightSideIdentifier);
		}
			
		int valueOfVar = 0;
		
		if (this.blockVars.get(blockHashCode).containsKey(identifier)) {
			valueOfVar = this.blockVars.get(blockHashCode).get(identifier);
			
			if (operator.equals("+="))
				this.blockVars.get(blockHashCode).replace(identifier, valueOfVar + value);
			else if (operator.equals("-="))
				this.blockVars.get(blockHashCode).replace(identifier, valueOfVar - value);
			else if (operator.equals("="))
				this.blockVars.get(blockHashCode).replace(identifier, value);
		}
	
		return true;
	}

	// Prefix expressions ex. ++x
	public boolean visit(PrefixExpression node)
	{
		int blockHashCode = ASTNodeUtils.getBlockDepth(node);
		String identifier = node.getOperand() + "";
		String operator = node.getOperator() + "";
		int valueOfVar = 0;
		
		if (this.blockVars.get(blockHashCode).containsKey(identifier)) {
			valueOfVar = this.blockVars.get(blockHashCode).get(identifier);
			
			if (operator.equals("++"))
				this.blockVars.get(blockHashCode).replace(identifier, valueOfVar + 1);
			else if (operator.equals("--"))
				this.blockVars.get(blockHashCode).replace(identifier, valueOfVar - 1);
		}

		return true;
	}
	
	// Postfix expressions ex. x++
	public boolean visit(PostfixExpression node)
	{
		int blockHashCode = ASTNodeUtils.getBlockDepth(node);
		String identifier = node.getOperand() + "";
		String operator = node.getOperator() + "";
		int valueOfVar = 0;
		
		if (this.blockVars.get(blockHashCode).containsKey(identifier)) {
			valueOfVar = this.blockVars.get(blockHashCode).get(identifier);
			
			if (operator.equals("++"))
				this.blockVars.get(blockHashCode).replace(identifier, valueOfVar + 1);
			else if (operator.equals("--"))
				this.blockVars.get(blockHashCode).replace(identifier, valueOfVar - 1);
		}

		return true;
	}
	
	// Infix expressions ex. x + y
	public int visitInfix(InfixExpression node)
	{
		int blockHashCode = ASTNodeUtils.getBlockDepth(node);
		String leftIdentifier = node.getLeftOperand() + "";
		String rightIdentifier = node.getRightOperand() + "";
		int leftSideValue = this.blockVars.get(blockHashCode).get(leftIdentifier) != null ? this.blockVars.get(blockHashCode).get(leftIdentifier) : Integer.parseInt(leftIdentifier);
		int rightSideValue = this.blockVars.get(blockHashCode).get(rightIdentifier) != null ? this.blockVars.get(blockHashCode).get(rightIdentifier) : Integer.parseInt(rightIdentifier);
		
		String operator = node.getOperator() + "";
		
		switch (operator) {
			case "+":
				return leftSideValue + rightSideValue;
			case "-":
				return leftSideValue - rightSideValue;
			case "*": 
				return leftSideValue * rightSideValue;
			case "/":
				return leftSideValue / rightSideValue;
			case "%":
				return leftSideValue % rightSideValue;
		}
		
		// Some dummy default return value, will never come here
		return 0;
	}
	
	public boolean visit(ReturnStatement node)
	{
		int value = 0;
		
		Expression expr = node.getExpression();
		
		// Calculating return value depending on expression type
		if (expr.getNodeType() == Type.NUMBER_LITERAL)
			value = Integer.parseInt(expr + "");
		else if (expr.getNodeType() == Type.INFIX_EXPRESSION)
			value = visitInfix((InfixExpression)expr);
		else if (expr.getNodeType() == Type.SIMPLE_NAME)
			value = this.blockVars.get(ASTNodeUtils.getBlockDepth(node)).get(expr + "");
		
		return true;
	}	
}
