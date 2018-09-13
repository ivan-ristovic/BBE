package bbe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.*;


@SuppressWarnings("unchecked")
public class SrcASTVisitor extends ASTVisitor
{
	private HashMap<Integer, BlockVariableMap> blockVars;

	
	public SrcASTVisitor()
	{
		this.blockVars = new HashMap<Integer, BlockVariableMap>();
		this.blockVars.put(ASTNodeUtils.ROOT_BLOCK_ID, new BlockVariableMap());
		ASTNodeUtils.resetCounters();
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
		
		Logger.logInfo("Adding block to map: " + id + " (parent: " + parId + ")");
		this.blockVars.put(id, new BlockVariableMap(this.blockVars.get(parId), null, node));
		
		return true;
	}

	public void endVisit(Block node) 
	{	
		Logger.logInfo("Exiting Block");
		
		int id = ASTNodeUtils.getBlockId(node);
		int parId = ASTNodeUtils.getBlockId(node.getParent());
		
		Logger.logInfo("Exiting Block: " + id);
		
		Iterator<Entry<String, Integer>> it = this.blockVars.get(id).entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
	        this.blockVars.get(parId).computeIfPresent(pair.getKey(), (k, v) -> pair.getValue());
	    }
	    
	    ASTNodeUtils.incrementBlockCount(node);
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
		// TODO if
		if (this.blockVars.get(ASTNodeUtils.getBlockId(node)).getInfixLogicalExpressionValue((InfixExpression)node.getExpression()))
			visit((Block)node.getThenStatement());
		else 
			if (node.getElseStatement() != null)
				visit((Block)node.getElseStatement());
		return false;
	}
}
