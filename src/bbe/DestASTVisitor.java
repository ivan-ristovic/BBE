package bbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		this.blockVars.put(ASTNodeUtils.ROOT_BLOCK_ID, new BlockVariableMap(renames));
		this.expectedVars = expectedVars;
		this.updates = renames;
		ASTNodeUtils.resetCounters();
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
		Logger.logInfo("Entering Block");

		int id = ASTNodeUtils.getBlockId(node);
		int parId = ASTNodeUtils.getBlockId(node.getParent());
		
		if (fatalError && !this.expectedVars.containsKey(parId)) {
			errorMessage = "block missmatch (" + parId + ")";
			return false;
		}
		
		Logger.logInfo("Adding block to map: " + id + " (parent: " + parId + ")");
		this.blockVars.put(id, new BlockVariableMap(this.blockVars.get(parId), updates, node));
		
		return true;
	}

	public void endVisit(Block node) 
	{	
		int id = ASTNodeUtils.getBlockId(node);
		int parId = ASTNodeUtils.getBlockId(node.getParent());
		
		Logger.logInfo("Exiting Block: " + id);
		
		Iterator<Entry<String, Integer>> it = this.blockVars.get(id).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
	        this.blockVars.get(parId).computeIfPresent(pair.getKey(), (k, v) -> pair.getValue());
	    }
	    
	    ArrayList<String> conflictingVars = new ArrayList<String>();
	    boolean hasBlockConflicts = false;
	    it = this.expectedVars.get(id).entrySet().iterator();
	    while (it.hasNext()) {		    
	        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
	    	String srcName = pair.getKey();
	    	Pair<String, String> rename = this.blockVars.get(id).getRenamePair(srcName);
	    	// If the name is not in the map, this indicates that it is the same in src and in dest.
	    	String destName = rename != null ? rename.second : srcName;
	    	if (destName == MappingFactory.MISSING) {
	    		Logger.logError("Variable (" + srcName + ") doesn't exist in dest.");
	    		// TODO should non-existing war be in conflicting vars?
	    		//conflictingVars.add(srcName);
				//hasBlockConflicts = true;
	    	}
	    	else {		    	
				int srcValue = this.expectedVars.get(id).get(srcName);
				int destValue = this.blockVars.get(id).get(destName);
				if (srcValue != destValue) {
					Logger.logError("Different value of variable: " + srcName + "(" + srcValue + ") != " + destName + "(" + destValue + ")");
					conflictingVars.add(srcName);
					hasBlockConflicts = true;
				}
	    	}
	    }
	    
	    Logger.logInfo("Block (" + id + ") traversed with conflicts: " + hasBlockConflicts);
	    if (hasBlockConflicts)
	    {
	    	Logger.logInfo("Conflicting vars: " + conflictingVars.toString());
	    	Logger.logInfo("Deep diving into into block (" + id + ")");
		    Block sourceBlock = this.expectedVars.get(id).getCurrentBlock();
		    compareBlocks(sourceBlock, node, conflictingVars);
	    }

	    ASTNodeUtils.incrementBlockCount(node);
	}
	
	private boolean compareBlocks(Block src, Block dest, ArrayList<String> conflictingVars) 
	{
		int id = ASTNodeUtils.getBlockId(src);
		Logger.logInfo("Comparing blocks with id (" + id + ") with conflicting vars (" + conflictingVars + ")");

		ArrayList<Statement> srcMatched = new ArrayList<Statement>();
		ArrayList<Statement> destMatched = new ArrayList<Statement>();
		
	    List<Statement> srcStatements = src.statements();
	    List<Statement> destStatements = dest.statements();
	    // Investigate further every conflicting var. Find all statements in which it appears in src,
	    // and in parallel search for the equivalent in the dest.
	    // Compare founded statements and determine the difference	    
		for (String conflictingVar : conflictingVars) {
			
			for (Statement srcStatement : srcStatements) {
				if (statementContainsVar(srcStatement, conflictingVar)) {
					
					for (Statement destStatement : destStatements) {
						if (destMatched.contains(destStatements))
							continue;
						
						if (statementContainsVar(destStatement, conflictingVar)) {
							//TODO compare statements
							boolean result = compareStatements(srcStatement, destStatement);
							if (result == false) {
								Logger.logError("Statements are different!" + srcStatement + " " + destStatement);
								// TODO handle this!
							}
							else
							{
								Logger.logError("Statements are the same! Adding to matched arrays: " + srcStatement + " " + destStatement);
								srcMatched.add(srcStatement);
								destMatched.add(destStatement);
								break;
							}
							break;						
						}
					}
					if (!srcMatched.contains(srcStatement)) {
						Logger.logError("No statement in dest that src statement '" + srcStatement + "' can comare to.");
					}					

				}				
			}
			// TODO check if there are statements in dest that don't exist in src
			// take unmatched dest statements and print error for them
		}
	    
		return true;
	}
	
	private boolean compareStatements(Statement src, Statement dest)
	{
		Logger.logInfo("Comparing statements: src: " + src + ",  dest: " + dest);
		
		// Has to be blockVars, because expectedVars throws nullPointer exception, don't have updates
		// We didn't make it with constructor that initializes updates
		BlockVariableMap map = this.blockVars.get(ASTNodeUtils.ROOT_BLOCK_ID);
		
		if (src.getNodeType() == Type.VARIABLE_DECLARATION_STATEMENT)
			map.checkDeclarationStatements((VariableDeclarationStatement)src, (VariableDeclarationStatement)dest);
		// This is the only way I found to make assignment from a statement
		else if (src.getNodeType() == Type.EXPRESSION_STATEMENT) {
			ExpressionStatement expressionStatementSrc = (ExpressionStatement)src;
			ExpressionStatement expressionStatementDest = (ExpressionStatement)dest;
			
			Expression e1 = expressionStatementSrc.getExpression();
			Expression e2 = expressionStatementDest.getExpression();
			
			if (expressionStatementSrc.getNodeType() == Type.ASSIGNMENT && expressionStatementDest.getNodeType() == Type.ASSIGNMENT) {
				map.checkAssignments((Assignment) e1, (Assignment)e2);
			}
			else if (expressionStatementSrc.getNodeType() == Type.ASSIGNMENT && expressionStatementDest.getNodeType() == Type.PREFIX_EXPRESSION) {
				map.checkAssignmentAndPrefixPostfix((Assignment) e1, (PrefixExpression)e2, "assignment");
			}
			else if (expressionStatementSrc.getNodeType() == Type.ASSIGNMENT && expressionStatementDest.getNodeType() == Type.POSTFIX_EXPRESSION) {
				map.checkAssignmentAndPrefixPostfix((Assignment) e1, (PostfixExpression)e2, "assignment");
			}
			else if (expressionStatementSrc.getNodeType() == Type.PREFIX_EXPRESSION && expressionStatementDest.getNodeType() == Type.ASSIGNMENT) {
				map.checkAssignmentAndPrefixPostfix((Assignment) e2, (PrefixExpression)e1, "expression");
			}
			else if (expressionStatementSrc.getNodeType() == Type.POSTFIX_EXPRESSION && expressionStatementDest.getNodeType() == Type.ASSIGNMENT) {
				map.checkAssignmentAndPrefixPostfix((Assignment) e2, (PostfixExpression)e1, "expression");
			}
		}
		
		return false;
	}
	
	private boolean statementContainsVar(Statement stmt, String var)
	{
		String[] split =  stmt.toString().split("\\b");
		for (String s : split)
			if (var.equals(s))
				return true;
		
		return false;
	}
	
	public boolean visit(VariableDeclarationStatement node) 
	{
		Logger.logInfo("Entering VariableDeclarationStatement");
		
		Type type = node.getType();
		for (Modifier modifier : (List<Modifier>)node.modifiers()) {
			// TODO check modifiers for each variable if they match in both files
			// if we want to support modifiers, then the var value in map has to be a class type :(
		}
		return true;
	}
	
	public boolean visit(VariableDeclarationFragment node) 
	{
		Logger.logInfo("Entering VariableDeclarationFragment: " + node.getName());
		
		int blockHashCode = ASTNodeUtils.getBlockId(node);
		SimpleName name = node.getName();
		Expression expr = node.getInitializer();
		
		int value = 0;
		
		// If declaration without initialization
		if (expr != null)
		{
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
			}
		}
		
		this.blockVars.get(blockHashCode).put(new String(name + ""), value);

		return false;
	}

	public boolean visit(Assignment node) 
	{
		Logger.logInfo("Entering Assignment");
		
		int blockHashCode = ASTNodeUtils.getBlockId(node);
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
		Logger.logInfo("Entering PrefixExpression");
		
		int blockHashCode = ASTNodeUtils.getBlockId(node);
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
		Logger.logInfo("Entering PostfixExpression");
		
		int blockHashCode = ASTNodeUtils.getBlockId(node);
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
		Logger.logInfo("Entering InfixExpression");
		
		int blockHashCode = ASTNodeUtils.getBlockId(node);
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
		Logger.logInfo("Entering ReturnStatement");
		
		int value = 0;
		
		Expression expr = node.getExpression();
		
		// Calculating return value depending on expression type
		if (expr.getNodeType() == Type.NUMBER_LITERAL)
			value = Integer.parseInt(expr + "");
		else if (expr.getNodeType() == Type.INFIX_EXPRESSION)
			value = visitInfix((InfixExpression)expr);
		else if (expr.getNodeType() == Type.SIMPLE_NAME)
			value = this.blockVars.get(ASTNodeUtils.getBlockId(node)).get(expr + "");
		
		this.blockVars.get(ASTNodeUtils.getBlockId(node)).put(ASTNodeUtils.getContainingMethodName(node), value);
		
		return true;
	}	
	
	public boolean visit(IfStatement node)
	{
		if (this.blockVars.get(ASTNodeUtils.ROOT_BLOCK_ID).getInfixLogicalExpressionValue((InfixExpression)node.getExpression()))
			visit((Block)node.getThenStatement());
		else 
			if (node.getElseStatement() != null)
				visit((Block)node.getElseStatement());
		return false;
	}
}
