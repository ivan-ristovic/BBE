package bbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.github.gumtreediff.utils.Pair;
import org.eclipse.jdt.core.dom.Block;

public class BlockVariableMap extends HashMap<String, Integer>
{
	private static final long serialVersionUID = 1L;
	private ArrayList<Pair<String, String>> updates;
	private Block currentBlock = null;

	public BlockVariableMap()
	{
		super();
		this.updates = new ArrayList<Pair<String, String>>();
	}
	
	public BlockVariableMap(ArrayList<Pair<String, String>> updates)
	{
		super();
		this.updates = updates;
	}
	
	public BlockVariableMap(BlockVariableMap map, ArrayList<Pair<String, String>> updates, Block currentBlock)
	{
		super(map);
		this.updates = updates;
		this.currentBlock = currentBlock;
	}

	public Block getCurrentBlock() {
		return currentBlock;
	}
	
	public Pair<String, String> getRenamePair(String variable1)
	{
		for (Pair<String, String> pair : updates)
			if (pair.first.equals(variable1)) {		
				return pair;
			}
	
		return null;
	}
	
	public Pair<String, String> getInfixPair(InfixExpression experssion)
	{
		String operand1 = experssion.getLeftOperand().toString();
		String operand2 = experssion.getRightOperand().toString();
	
		String pair1 = getPair(operand1);
		String pair2 = getPair(operand2);
		
		Pair<String, String> pair = new Pair<String, String>(pair1, pair2);
		
		return pair;
	}
	
	public String getPair(String variable)
	{    
		for (Pair<String, String> pair : updates)
			if (pair.first.equals(variable))		
				return pair.second;
			else if (pair.second.equals(variable))
				return pair.first;
		
		return null;
	}
	
	public boolean checkVariables(String variable1, String variable2)
	{
		if (variable1.equals(variable2))
			return true;
			
		// We are looking for the first one (one from the correct code)
		Pair<String, String> variablePair = this.getRenamePair(variable1);
		if (variablePair == null) 
			return false;
		
		// If the variable from wrong code is not the same (exactly or at least renamed)
		// as the one in correct code error
		if (!variablePair.second.equals(variable2)) {
			return false;
		}
	
		return true;
	}

	public boolean checkNumbers(Expression expr1, Expression expr2)
	{
		// If numbers are different error
		if (Integer.parseInt(expr1.toString()) != Integer.parseInt(expr2.toString())) {
			return false;
		}
		
		return true;
	}
	
	public boolean checkVariableAndNumber(String variable, Expression expr)
	{
//		Iterator<Entry<String, Integer>> it = this.entrySet().iterator();
//	    while (it.hasNext()) {
//	    	Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
//	        System.err.println("Pair: " + pair.getKey() + " " + pair.getValue());
//	    }
	    
	    int value = this.containsKey(variable) ? this.get(variable) : this.get(getPair(variable));
	    
		// If variable has different value than number
		if (Integer.parseInt(expr.toString()) != value)
			return false;
		
		return true;
	}
	
	public boolean getInfixLogicalExpressionValue (InfixExpression expression)
	{
		Expression left = expression.getLeftOperand();
		Expression right = expression.getRightOperand();
		String operator = expression.getOperator().toString();
		
		int valueLeft = 0, valueRight = 0; 
		
		if (left.getNodeType() == Type.NUMBER_LITERAL)
			valueLeft = Integer.parseInt(left.toString());
		else if (left.getNodeType() == Type.SIMPLE_NAME)
		{
			valueLeft = this.containsKey(left.toString()) ? this.get(left.toString()) : this.get(getPair(left.toString()));
		}
		else if (left.getNodeType() == Type.INFIX_EXPRESSION)
			valueLeft = getInfixExpressionValue((InfixExpression)left);
		
		if (right.getNodeType() == Type.NUMBER_LITERAL)
			valueRight = Integer.parseInt(right.toString());
		else if (right.getNodeType() == Type.SIMPLE_NAME)
			valueRight = this.containsKey(right.toString()) ? this.get(right.toString()) : this.get(getPair(right.toString()));
		else if (right.getNodeType() == Type.INFIX_EXPRESSION)
			valueRight = getInfixExpressionValue((InfixExpression)right);
		
		boolean value = false;
		
		switch(operator) {
			case "<":
				value = valueLeft < valueRight;
				break;
			case ">":
				value = valueLeft > valueRight;
				break;
			case "<=":
				value = valueLeft <= valueRight;
				break;
			case ">=":
				value = valueLeft >= valueRight;
				break;
			case "==":
				value = valueLeft == valueRight;
				break;
			case "!=":
				value = valueLeft != valueRight;
				break;
		}
		
		return value;
	}
	
	public int getInfixExpressionValue(InfixExpression expression)
	{
		Expression left = expression.getLeftOperand();
		Expression right = expression.getRightOperand();
		
		int valueLeft, valueRight;

		// TODO remove
		Logger.logInfo("EXPRESSION: " + expression.toString());
		Logger.logInfo("OPERANDSS: " + left.toString() + " | " + right.toString());
		
		if (left.getNodeType() == Type.INFIX_EXPRESSION)
			valueLeft = getInfixExpressionValue((InfixExpression)left);
		else if (left.getNodeType() == Type.NUMBER_LITERAL)
			valueLeft = Integer.parseInt(left.toString());
		else 
			valueLeft = (this.containsKey(left.toString()) ? this.get(left.toString()) : this.get(getPair(left.toString())));

		if (right.getNodeType() == Type.INFIX_EXPRESSION)
			valueRight = getInfixExpressionValue((InfixExpression)right);
		else if (right.getNodeType() == Type.NUMBER_LITERAL) 
			valueRight = Integer.parseInt(right.toString());
		else
			valueRight = (this.containsKey(right.toString()) ? this.get(right.toString()) : this.get(getPair(right.toString())));
		
		int value = 0;
		
		if (expression.getOperator() == InfixExpression.Operator.PLUS)
			value = valueLeft + valueRight;
		else if (expression.getOperator() == InfixExpression.Operator.MINUS)
			value = valueLeft - valueRight;
		else if (expression.getOperator() == InfixExpression.Operator.TIMES)
			value = valueLeft * valueRight;
		else if (expression.getOperator() == InfixExpression.Operator.DIVIDE)
			value = valueLeft / valueRight;
		else if (expression.getOperator() == InfixExpression.Operator.REMAINDER)
			value = valueLeft % valueRight;
		
		return value;
	}
	
	public boolean checkInfixAndVariable(InfixExpression expression, String variable)
	{
		int value = getInfixExpressionValue(expression);
	
		if (value != (this.containsKey(variable) ? this.get(variable) : this.get(getPair(variable)))) {
			return false;
		}
		
		return true;
	}
	
	public boolean checkInfixAndNumber(InfixExpression expression, Expression number)
	{
		int value = getInfixExpressionValue(expression);
	
		if (value != Integer.parseInt(number.toString())) {
			return false;
		}
		
		return true;
	}
	
	// Maybe it can be void, if we write to file or smth.
	// Even if it is failure, we are continuing recursive search of nodes
	public boolean checkAssignments(Assignment node1, Assignment node2)
	{
		// Taking variables
		String variable1 = node1.getLeftHandSide().toString();
		String variable2 = node2.getLeftHandSide().toString();
		
		String operator1 = node1.getOperator().toString();
		String operator2 = node2.getOperator().toString();
		
		String replacement = "";
		
		boolean failure = false;
		
		if (!operator1.equals(operator2)) {
			System.out.println(operator2 + " should be replaced with " + operator1);
			failure = true;
		}
		
		if (!checkVariables(variable1, variable2)) { 
			System.out.println(variable2 + " should be replaced with " + 
					(this.containsKey(variable1) ? variable1 : getPair(variable1)) + " or " + 
					(this.containsKey(variable1) ? this.get(variable1) : this.get(getPair(variable1))));
			/**** NO return here, we still want to check right sides ****/
			failure = true;
		}
		
		Expression initializer1 = node1.getRightHandSide();
		Expression initializer2 = node2.getRightHandSide();
		
		if (initializer1.getNodeType() == Type.NUMBER_LITERAL) {
			// Both initializers are numbers
			if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkNumbers(initializer1, initializer2)) {
					System.out.println(initializer2 + " should be replaced with " + initializer1);
					failure = true;
				}
			}
			else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkVariableAndNumber(initializer2.toString(), initializer1)) {
					System.out.println(initializer2 + " should be replaced with " + initializer1);
					failure = true;
				}
			}
			else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfixAndNumber((InfixExpression)initializer2, initializer1)) {
					System.out.println(initializer2 + " should be replaced with " + initializer1);
					failure = true;
				}
			}
		}
		else if (initializer1.getNodeType() == Type.SIMPLE_NAME) {
			if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkVariableAndNumber(initializer1.toString(), initializer2)) {
					System.out.println(initializer2 + " should be replaced with " + 
							(this.containsKey(initializer1) ? initializer1 : getPair(initializer1.toString()))
							+ " or " + 
							(this.containsKey(initializer1) ? this.get(initializer1) : this.get(getPair(initializer1.toString()))));
					failure = true;
				}
			}
			else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkVariables(initializer1.toString(), initializer2.toString())) {
					System.out.println(initializer2 + " should be replaced with variable " + 
							(this.containsKey(initializer1) ? initializer1 : getPair(initializer1.toString()))
							+ " or " + 
							(this.containsKey(initializer1) ? this.get(initializer1) : this.get(getPair(initializer1.toString()))));
					failure = true;
				}
			}
			else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfixAndVariable((InfixExpression)initializer2, initializer1.toString())) {
					System.out.println(initializer2 + " should be replaced with " + 
							(this.containsKey(initializer1) ? initializer1 : getPair(initializer1.toString()))
							+ " or " + 
							(this.containsKey(initializer1) ? this.get(initializer1) : this.get(getPair(initializer1.toString()))));
					failure = true;
				}
			}
		}
		
		return !failure;
	}
	
	// Again maybe void
	public boolean checkInfix(InfixExpression expr1, InfixExpression expr2)
	{
		Expression left1 = expr1.getLeftOperand();
		Expression left2 = expr2.getLeftOperand();
		Expression right1 = expr1.getRightOperand();
		Expression right2 = expr2.getRightOperand();
		
		String operator1 = expr1.getOperator().toString();
		String operator2 = expr2.getOperator().toString();
							
		String replacement = "";
		
		boolean failure = false;
		
		if (!operator1.equals(operator2)) {
			System.out.println(operator1 + " should be replaced with " + operator2);
			failure = true;
		}

		if (left1.getNodeType() == Type.NUMBER_LITERAL) {
			if (left2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkNumbers(left1, left2)) {
					System.out.println(left2 + " should be replaced with " + left1);
					failure = true;
				}
			}
			else if (left2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkVariableAndNumber(left2.toString(), left1)) {
					System.out.println(left2 + " should be replaced with " + left1);
					failure = true;
				}
			}
			else if (left2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfixAndNumber((InfixExpression)left2, left1)) {
					System.out.println(left2 + " should be replaced with " + left1);
					failure = true;
				}
			}
		}
		else if (left1.getNodeType() == Type.SIMPLE_NAME) {
			if (left2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkVariableAndNumber(left1.toString(), left2)) {
					System.out.println(left2 + " should be replaced with " + 
							(this.containsKey(left1) ? left1 : getPair(left1.toString()))
							+ " or " + 
							(this.containsKey(left1) ? this.get(left1) : this.get(getPair(left1.toString()))));
					failure = true;
				}
			}
			else if (left2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkVariables(left1.toString(), left2.toString())) {
					System.out.println(left2 + " should be replaced with " +
							(this.containsKey(left1) ? left1 : getPair(left1.toString()))
							+ " or " + 
							(this.containsKey(left1) ? this.get(left1) : this.get(getPair(left1.toString()))));
					failure = true;
				}
			}
			else if (left2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfixAndVariable((InfixExpression)left2, left1.toString())) {
					System.out.println(left2 + " should be replaced with " + 
							(this.containsKey(left1) ? left1 : getPair(left1.toString()))
							+ " or " + 
							(this.containsKey(left1) ? this.get(left1) : this.get(getPair(left1.toString()))));
					failure = true;
				}
			}
		}
		else if (left1.getNodeType() == Type.INFIX_EXPRESSION) {
			Pair<String, String> infixPair = getInfixPair((InfixExpression)left1);
			InfixExpression expr = (InfixExpression)left1;
			String operator = expr.getOperator().toString();
			
			if (left2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkInfixAndNumber((InfixExpression)left1, left2)) {
					System.out.println(left2 + " should be replaced with " + 
							infixPair.first + " " + operator + " " + infixPair.second + " or " +
							getInfixExpressionValue((InfixExpression)left1));
					failure = true;
				}
			}
			else if (left2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkInfixAndVariable((InfixExpression)left1, left2.toString())) {
					System.out.println(left2 + " should be replaced with " + 
							infixPair.first + " " + operator + " " + infixPair.second + " or " +
							getInfixExpressionValue((InfixExpression)left1));
					failure = true;
				}
			}
			else if (left2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfix((InfixExpression)left1, (InfixExpression)left2)) {
					System.out.println(left2 + " should be replaced with " + 
							infixPair.first + " " + operator + " " + infixPair.second + " or " +
							getInfixExpressionValue((InfixExpression)left1));
					failure = true;
				}
			}
		}
		
		if (right1.getNodeType() == Type.NUMBER_LITERAL) {
			if (right2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkNumbers(right1, right2)) {
					System.out.println(right2 + " should be replaced with " + right1);
					failure = true;
				}
			}
			else if (right2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkVariableAndNumber(right2.toString(), right1)) {
					System.out.println(right2 + " should be replaced with " + right1);
					failure = true;
				}
			}
			else if (right2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfixAndNumber((InfixExpression)right2, right1)) {
					System.out.println(right2 + " should be replaced with " + right1);
					failure = true;
				}
			}
		}
		else if (right1.getNodeType() == Type.SIMPLE_NAME) {
			if (right2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkVariableAndNumber(right1.toString(), right2)) {
					System.out.println(right2 + " should be replaced with " +
							(this.containsKey(right1) ? right1 : getPair(right1.toString()))
							+ " or " + 
							(this.containsKey(right1) ? this.get(right1) : this.get(getPair(right1.toString()))));
					failure = true;
				}
			}
			else if (right2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkVariables(right1.toString(), right2.toString())) {
					System.out.println(right2 + " should be replaced with " + 
							(this.containsKey(right1) ? right1 : getPair(right1.toString()))
							+ " or " + 
							(this.containsKey(right1) ? this.get(right1) : this.get(getPair(right1.toString()))));
					failure = true;
				}
			}
			else if (right2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfixAndVariable((InfixExpression)right2, right1.toString())) {
					System.out.println(right2 + " should be replaced with " + 
							(this.containsKey(right1) ? right1 : getPair(right1.toString()))
							+ " or " + 
							(this.containsKey(right1) ? this.get(right1) : this.get(getPair(right1.toString()))));
					failure = true;
				}
			}
		}
		else if (right1.getNodeType() == Type.INFIX_EXPRESSION) {
			Pair<String, String> infixPair = getInfixPair((InfixExpression)right1);
			InfixExpression expr = (InfixExpression)right1;
			String operator = expr.getOperator().toString();
			
			if (right2.getNodeType() == Type.NUMBER_LITERAL) {
				if (!checkInfixAndNumber((InfixExpression)right1, right2)) {
					System.out.println(right2 + " should be replaced with " + 
							infixPair.first + " " + operator + " " + infixPair.second + " or " +
							getInfixExpressionValue((InfixExpression)right1));
					failure = true;
				}
			}
			else if (right2.getNodeType() == Type.SIMPLE_NAME) {
				if (!checkInfixAndVariable((InfixExpression)right1, right2.toString())) {
					System.out.println(right2 + " should be replaced with " +
							infixPair.first + " " + operator + " " + infixPair.second + " or " +
							getInfixExpressionValue((InfixExpression)right1));
					failure = true;
				}
			}
			else if (right2.getNodeType() == Type.INFIX_EXPRESSION) {
				if (!checkInfix((InfixExpression)right1, (InfixExpression)right2)) {
					System.out.println(right2 + " should be replaced with " + 
							infixPair.first + " " + operator + " " + infixPair.second + " or " +
							getInfixExpressionValue((InfixExpression)right1));
					failure = true;
				}
			}
		}
		
		return !failure;
	}

	public boolean checkDeclarationStatements(VariableDeclarationStatement declaration1, VariableDeclarationStatement declaration2)
	{
		List<VariableDeclarationFragment> fragments1 = declaration1.fragments();
		List<VariableDeclarationFragment> fragments2 = declaration2.fragments();
		
		int len1 = fragments1.size();
		int len2 = fragments2.size();
		
		int len = len1 <= len2 ? len1 : len2;
		
		boolean failure = false;
		
		for (int i = 0; i < len; i++) {
			if (!failure)
				failure = checkDeclarations(fragments1.get(i), fragments2.get(i));
			else
				checkDeclarations(fragments1.get(i), fragments2.get(i));
		}
		
		// If we want to cover those cases
		if (len1 > len2) {
			for (int i = len2; i < len1; i++) {
				System.out.println("Declaration of variable " + fragments1.get(i) + " should be added");
			}
		}
		else if (len2 > len1) {
			for (int i = len1; i < len2; i++) {
				System.out.println("Declaration of variable " + fragments2.get(i) + " is extra");
			}
		}
		
		return !failure;
	}
	
	public boolean checkDeclarations(VariableDeclarationFragment declaration1, VariableDeclarationFragment declaration2)
	{
		Expression initializer1 = declaration1.getInitializer();
		Expression initializer2 = declaration2.getInitializer();
		
		String replacement = "";
		
		boolean failure = false;
		
		// If we have one only initializer
		if (initializer1 != null && initializer2 == null) {
			System.out.println(initializer1 + " should be added");
			failure = true;
		}
		else if (initializer1 == null && initializer2 != null) {
			System.out.println(initializer2 + " is extra");
			failure = true;
		}
		else if (initializer1 != null && initializer2 != null) {
			if (initializer1.getNodeType() == Type.NUMBER_LITERAL) {
				if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
					if (!checkNumbers(initializer1, initializer2)) {
						System.out.println(initializer2 + " should be replaced with " + initializer1);
						failure = true;
					}
				}
				else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
					if (!checkVariableAndNumber(initializer2.toString(), initializer1)) {
						System.out.println(initializer2 + " should be replaced with " + initializer1);
						failure = true;
					}
				}
				else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
					if (!checkInfixAndNumber((InfixExpression)initializer2, initializer1)) {
						System.out.println(initializer2 + " should be replaced with " + initializer1);
						failure = true;
					}
				}
			}
			else if (initializer1.getNodeType() == Type.SIMPLE_NAME) {
				if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
					if (!checkVariableAndNumber(initializer1.toString(), initializer2)) {
						System.out.println(initializer2 + " should be replaced with " +
								(this.containsKey(initializer1) ? initializer1 : getPair(initializer1.toString()))
								+ " or " + 
								(this.containsKey(initializer1) ? this.get(initializer1) : this.get(getPair(initializer1.toString()))));
						failure = true;
					}
				}
				else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
					if (!checkVariables(initializer1.toString(), initializer2.toString())) {
						System.out.println(initializer2 + " should be replaced with " + 
								(this.containsKey(initializer1) ? initializer1 : getPair(initializer1.toString()))
								+ " or " + 
								(this.containsKey(initializer1) ? this.get(initializer1) : this.get(getPair(initializer1.toString()))));
						failure = true;
					}
				}
				else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
					if (!checkInfixAndVariable((InfixExpression)initializer2, initializer1.toString())) {
						System.out.println(initializer2 + " should be replaced with " + 
								(this.containsKey(initializer1) ? initializer1 : getPair(initializer1.toString()))
								+ " or " + 
								(this.containsKey(initializer1) ? this.get(initializer1) : this.get(getPair(initializer1.toString()))));
						failure = true;
					}
				}
			}
			else if (initializer1.getNodeType() == Type.INFIX_EXPRESSION) {
				Pair<String, String> infixPair = getInfixPair((InfixExpression)initializer1);
				InfixExpression expr = (InfixExpression)initializer1;
				String operator = expr.getOperator().toString();
				
				if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
					if (!checkInfixAndNumber((InfixExpression)initializer1, initializer2)) {
						System.out.println(initializer2 + " should be replaced with " + 
								infixPair.first + " " + operator + " " + infixPair.second + " or " +
								getInfixExpressionValue((InfixExpression)initializer1));
						failure = true;
					}
				}
				else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
					if (!checkInfixAndVariable((InfixExpression)initializer1, initializer2.toString())) {
						System.out.println(initializer2 + " should be replaced with " + 
								infixPair.first + " " + operator + " " + infixPair.second + " or " +
								getInfixExpressionValue((InfixExpression)initializer1));
						failure = true;
					}
				}
				else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
					if (!checkInfix((InfixExpression)initializer1, (InfixExpression)initializer2)) {
						System.out.println(initializer2 + " should be replaced with " + 
								infixPair.first + " " + operator + " " + infixPair.second + " or " +
								getInfixExpressionValue((InfixExpression)initializer1));
						failure = true;
					}
				}
			}
		}
		
		return !failure;
	}
	
	// In functions before, we knew what statement was from what (correct or wrong) code, 
	// Here we don't, so we have and indicator that says if assignment or expression is from correct code
	// Possible values for indicator are assignment and expression
	// Function now works only for cases where += 1 or -= 1 like x += 1 / a++ or x -= 1 / a--
	public boolean checkAssignmentAndPrefixPostfix(Assignment assignment, Expression expression, String indicator) 
	{
		boolean failure = false;
		
		String assignmentOperator = assignment.getOperator().toString();
		String variable1 = assignment.getLeftHandSide().toString();
		
		String prefixPostfixOperator = "";
		String variable2 = "";
		
		String replacement = "";

		int initializer = 0;
		
		Expression rightSide = assignment.getRightHandSide();
		
		if (rightSide.getNodeType() == Type.NUMBER_LITERAL)
			initializer = Integer.parseInt(rightSide.toString());
		else if (rightSide.getNodeType() == Type.SIMPLE_NAME) 
			initializer = this.containsKey(rightSide) ? this.get(rightSide) : this.get(getPair(rightSide.toString()));

		if (expression instanceof PrefixExpression) {
			prefixPostfixOperator = ((PrefixExpression)expression).getOperator().toString();
			variable2 = ((PrefixExpression)expression).getOperand().toString();
		}
		
		if (expression instanceof PostfixExpression) {
			prefixPostfixOperator = ((PostfixExpression)expression).getOperator().toString();
			variable2 = ((PostfixExpression)expression).getOperand().toString();
		}
		
		if (indicator.equals("assignment")) {
			if (assignmentOperator.equals("=")) {
				System.out.println(prefixPostfixOperator + " should be replaced with " + assignmentOperator);
				failure = true;
			}
			if (assignmentOperator.equals("+=")) {
				if (prefixPostfixOperator.equals("--")) {
					if (initializer == 1)
						System.out.println(prefixPostfixOperator + " should be replaced with ++");
					else 
						System.out.println(prefixPostfixOperator + " should be replaced with " + initializer + " times ++ or +=" + initializer);
					failure = true;
				}
			}
			else if (assignmentOperator.equals("-=")) {
				if (prefixPostfixOperator.equals("++")) {
					if (initializer == 1)
						System.out.println(prefixPostfixOperator + " should be replaced with --");
					else 
						System.out.println(prefixPostfixOperator + " should be replaced with " + initializer + " times -- or -=" + initializer);
					failure = true;
				}
			}
			
			if (!checkVariables(variable1, variable2)) {
				System.out.println(variable2 + " should be replaced with " + 
						(this.containsKey(variable1) ? variable1 : getPair(variable1.toString()))
						+ " or " + 
						(this.containsKey(variable1) ? this.get(variable1) : this.get(getPair(variable1.toString()))));
				failure = true;
			}
		}
		else {
			if (assignmentOperator.equals("=")) {
				System.out.println(assignmentOperator + " should be replaced with " + prefixPostfixOperator);
				failure = true;
			}
			if (assignmentOperator.equals("+=")) {
				if (prefixPostfixOperator.equals("--")) {
					System.out.println(assignmentOperator + " should be replaced with -= 1");
					failure = true;
				}
			}
			else if (assignmentOperator.equals("-=")) {
				if (prefixPostfixOperator.equals("++")) {
					System.out.println(assignmentOperator + " should be replaced with += 1");
					failure = true;
				}
			}
			
			if (!checkVariables(variable1, variable2)) {
				System.out.println(variable1 + " should be replaced with " +
						(this.containsKey(variable2) ? variable2 : getPair(variable2.toString()))
						+ " or " + 
						(this.containsKey(variable2) ? this.get(variable2) : this.get(getPair(variable2.toString()))));
				failure = true;
			}
		}
			
		return !failure;
	}
}
