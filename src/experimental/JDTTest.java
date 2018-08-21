package experimental;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.*;

import com.github.gumtreediff.utils.Pair;

public class JDTTest 
{
	
	public static void main(String[] args) 
	{
		Scanner sc = null;
		try {
			sc = new Scanner(new File("tests/test1.java"));
			String src = sc.useDelimiter("\\A").next();

			ASTParser parser = ASTParser.newParser(AST.JLS8); 
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(src.toCharArray());
			parser.setResolveBindings(true);
			
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

			MappingFactory mFactory = new MappingFactory("tests/test1.java", "tests/test2.java");
			
			ArrayList<Pair<String, String>> updates = mFactory.getUpdates();
			
			cu.accept(new ASTVisitor() {
	 
				HashMap<String, Integer> vars = new HashMap<String, Integer>();

				@SuppressWarnings("unchecked")
				public boolean visit(VariableDeclarationStatement node) {
					Type type = node.getType();
					StringBuilder sb = new StringBuilder();
					for (Modifier m : (List<Modifier>)node.modifiers()) {
						sb.append(m.getKeyword().toString());
						sb.append(' ');
					}
					System.out.println("Declaration of type '" + type.toString() + "' with modifiers: " + sb.toString());
					return true;
				}
				
				public boolean visit(VariableDeclarationFragment node) {
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
							if (this.vars.containsKey(expr + ""))
								value = this.vars.get(expr + "");
							else 
								value = Integer.MAX_VALUE;
						}
					}
					
					vars.put(new String(name + ""), value);
					
					System.out.println("Declaration of '" + name.getIdentifier() + "' at line " + cu.getLineNumber(name.getStartPosition()) + " with value " + this.vars.get(name.getIdentifier()));
					
					return false;
				}
	 
//				public boolean visit(SimpleName node) {
//					if (this.vars.containsKey(node.getIdentifier())) {
//						System.out.println("Usage of '" + node + "' with value " + vars.get(node.getIdentifier() + " at line " +	cu.getLineNumber(node.getStartPosition())));
//					}
//					return true;
//				}
	 
				public boolean visit(Assignment node) {
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
						value = this.vars.get(rightSideIdentifier);
					}
						
					int valueOfVar = 0;
					
					if (this.vars.containsKey(identifier)) {
						valueOfVar = this.vars.get(identifier);
						
						if (operator.equals("+="))
							this.vars.replace(identifier, valueOfVar + value);
						else if (operator.equals("-="))
							this.vars.replace(identifier, valueOfVar - value);
						else if (operator.equals("="))
							this.vars.replace(identifier, value);
					}
				
					System.out.println("Assignment, change variable " + identifier + " value from " + valueOfVar +
							" to " + this.vars.get(identifier)  + " at line " + cu.getLineNumber(node.getStartPosition()));
					return true;
				}

				// Prefix expressions ex. ++x
				public boolean visit(PrefixExpression node)
				{
					String identifier = node.getOperand() + "";
					String operator = node.getOperator() + "";
					int valueOfVar = 0;
					
					if (this.vars.containsKey(identifier)) {
						valueOfVar = this.vars.get(identifier);
						
						if (operator.equals("++"))
							this.vars.replace(identifier, valueOfVar + 1);
						else if (operator.equals("--"))
							this.vars.replace(identifier, valueOfVar - 1);
					}

					System.out.println("PrefixExpression, change variable " + identifier + " value from " + valueOfVar +
							" to " + this.vars.get(identifier) + " at line " + cu.getLineNumber(node.getStartPosition()));
					return true;
				}
				
				// Postfix expressions ex. x++
				public boolean visit(PostfixExpression node)
				{
					String identifier = node.getOperand() + "";
					String operator = node.getOperator() + "";
					int valueOfVar = 0;
					
					if (this.vars.containsKey(identifier)) {
						valueOfVar = this.vars.get(identifier);
						
						if (operator.equals("++"))
							this.vars.replace(identifier, valueOfVar + 1);
						else if (operator.equals("--"))
							this.vars.replace(identifier, valueOfVar - 1);
					}

					System.out.println("PostfixExpression, change variable " + identifier + " value from " + valueOfVar +
							" to " + this.vars.get(identifier) + " at line " + cu.getLineNumber(node.getStartPosition()));
					return true;
				}
				
				// Infix expressions ex. x + y
				public int visitInfix(InfixExpression node)
				{
					String leftIdentifier = node.getLeftOperand() + "";
					String rightIdentifier = node.getRightOperand() + "";
					int leftSideValue = this.vars.get(leftIdentifier) != null ? this.vars.get(leftIdentifier) : Integer.parseInt(leftIdentifier);
					int rightSideValue = this.vars.get(rightIdentifier) != null ? this.vars.get(rightIdentifier) : Integer.parseInt(rightIdentifier);
					
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
				
				public boolean visit(Block node) {
					System.out.println("--- BLOCK ---");
					// perform var checks here
					
					return true;
				}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				// Function that looks for index of variable pair based on first variable name
				private int findVariablesPair(String variable1)
				{
					int i = 0;
					
					for (Pair<String, String> pair : updates) {
						if (pair.first.equals(variable1))
							return i;
						i++;
					}
					
					return -1;
				}
				
				public boolean checkVariables(String variable1, String variable2, String replacement)
				{
					// We are looking for the first one (one from the correct code)
					int index = findVariablesPair(variable1);
					
					Pair<String, String> variablePair = null;
					
					if (index == -1) 
						return false;
					
					// Taking the right pair of variables from updates
					variablePair = updates.get(index);
					
					// If the variable from wrong code is not the same (exactly or at least renamed)
					// as the one in correct code error
					if (!variablePair.second.equals(variable2)) {
						replacement = variablePair.second;
						return false;
					}
					
					return true;
				}
				
				public boolean checkNumbers (Expression expr1, Expression expr2)
				{
					// If numbers are different error
					if (Integer.parseInt(expr1.toString()) != Integer.parseInt(expr2.toString())) {
						return false;
					}
					
					return true;
				}
				
				public boolean checkVariableAndNumber(String variable, Expression expr)
				{
					// If variable has different value than number
					if (Integer.parseInt(expr.toString()) != this.vars.get(variable))
						return false;
					
					return true;
				}
				
				private int getInfixExpressionValue(InfixExpression expression)
				{
					Expression left = expression.getLeftOperand();
					Expression right = expression.getRightOperand();
					String operator = expression.getOperator().toString();
					
					int valueLeft = left.getNodeType() == Type.NUMBER_LITERAL ? Integer.parseInt(left.toString()) : this.vars.get(left.toString());
					int valueRight = right.getNodeType() == Type.NUMBER_LITERAL ? Integer.parseInt(right.toString()) : this.vars.get(right.toString());
					
					int value = 0;
					
					switch (operator) {
						case "+":
							value = valueLeft + valueRight;
							break;
						case "-":
							value = valueLeft - valueRight;
							break;
						case "*":
							value = valueLeft * valueRight;
							break;
						case "/":
							value = valueLeft / valueRight;
							break;
						case "%":
							value = valueLeft % valueRight;
							break;
					}
					
					return value;
				}
				
				public boolean checkInfixAndVariable(InfixExpression expression, String variable)
				{
					int value = getInfixExpressionValue(expression);
				
					if (value != this.vars.get(variable)) {
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
					
					if (!checkVariables(variable1, variable2, replacement)) { 
						System.out.println("Wrong variable in assignment, variable " + variable2 + " should" +
								"be replaced with variable" + replacement);
						/**** NO return here, we still want to check right sides ****/
						failure = true;
					}
					
					Expression initializer1 = node1.getRightHandSide();
					Expression initializer2 = node2.getRightHandSide();
					
					if (initializer1.getNodeType() == Type.NUMBER_LITERAL) {
						// Both initializers are numbers
						if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
							if (!checkNumbers(initializer1, initializer2)) {
								System.out.println("Wrong initializer in assignment, initializer " + initializer2 + " should" +
										"be replaced with initializer " + initializer1);
								failure = true;
							}
						}
						else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
							if (!checkVariableAndNumber(initializer2.toString(), initializer1)) {
								System.out.println("Wrong variable in assignment, variable " + initializer2 + " should" +
										"be replaced with initializer " + initializer1);
								failure = true;
							}
						}
						else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
							if (!checkInfixAndNumber((InfixExpression)initializer2, initializer1)) {
								System.out.println("Wrong variable in assignment, initializer " + initializer2 + " should" +
										"be replaced with initializer " + initializer1);
								failure = true;
							}
						}
					}
					else if (initializer1.getNodeType() == Type.SIMPLE_NAME) {
						if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
							if (!checkVariableAndNumber(initializer1.toString(), initializer2)) {
								System.out.println("Wrong initializer in assignment, initializer " + initializer2 + " should" +
										"be replaced with variable " + initializer1);
								failure = true;
							}
						}
						else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
							if (!checkVariables(initializer1.toString(), initializer2.toString(), replacement)) {
								System.out.println("Wrong variable in assignment, variable " + initializer2 + " should" +
										"be replaced with variable " + replacement);
								failure = true;
							}
						}
						else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
							if (!checkInfixAndVariable((InfixExpression)initializer2, initializer1.toString())) {
								System.out.println("Wrong variable in assignment, initializer " + initializer2 + " should" +
										"be replaced with initializer " + initializer1);
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
								System.out.println(left2 + " should be replaced with " + left1);
								failure = true;
							}
						}
						else if (left2.getNodeType() == Type.SIMPLE_NAME) {
							if (!checkVariables(left1.toString(), left2.toString(), replacement)) {
								System.out.println(left2 + " should be replaced with " + replacement);
								failure = true;
							}
						}
						else if (left2.getNodeType() == Type.INFIX_EXPRESSION) {
							if (!checkInfixAndVariable((InfixExpression)left2, left1.toString())) {
								System.out.println(left2 + " should be replaced with " + left1);
								failure = true;
							}
						}
					}
					else if (left1.getNodeType() == Type.INFIX_EXPRESSION) {
						if (left2.getNodeType() == Type.NUMBER_LITERAL) {
							if (!checkInfixAndNumber((InfixExpression)left1, left2)) {
								System.out.println(left2 + " should be replaced with " + left1);
								failure = true;
							}
						}
						else if (left2.getNodeType() == Type.SIMPLE_NAME) {
							if (!checkInfixAndVariable((InfixExpression)left1, left2.toString())) {
								System.out.println(left2 + " should be replaced with " + replacement);
								failure = true;
							}
						}
						else if (left2.getNodeType() == Type.INFIX_EXPRESSION) {
							if (!checkInfix((InfixExpression)left1, (InfixExpression)left2)) {
								System.out.println(left2 + " should be replaced with " + left1);
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
								System.out.println(right2 + " should be replaced with " + right1);
								failure = true;
							}
						}
						else if (right2.getNodeType() == Type.SIMPLE_NAME) {
							if (!checkVariables(right1.toString(), right2.toString(), replacement)) {
								System.out.println(right2 + " should be replaced with " + replacement);
								failure = true;
							}
						}
						else if (right2.getNodeType() == Type.INFIX_EXPRESSION) {
							if (!checkInfixAndVariable((InfixExpression)right2, right1.toString())) {
								System.out.println(right2 + " should be replaced with " + right1);
								failure = true;
							}
						}
					}
					else if (right1.getNodeType() == Type.INFIX_EXPRESSION) {
						if (right2.getNodeType() == Type.NUMBER_LITERAL) {
							if (!checkInfixAndNumber((InfixExpression)right1, right2)) {
								System.out.println(right2 + " should be replaced with " + right1);
								failure = true;
							}
						}
						else if (right2.getNodeType() == Type.SIMPLE_NAME) {
							if (!checkInfixAndVariable((InfixExpression)right1, right2.toString())) {
								System.out.println(right2 + " should be replaced with " + replacement);
								failure = true;
							}
						}
						else if (right2.getNodeType() == Type.INFIX_EXPRESSION) {
							if (!checkInfix((InfixExpression)right1, (InfixExpression)right2)) {
								System.out.println(right2 + " should be replaced with " + right1);
								failure = true;
							}
						}
					}
					
					return !failure;
				}

				public boolean checkDeclarations(VariableDeclarationFragment declaration1, VariableDeclarationFragment declaration2)
				{
					Expression initializer1 = declaration1.getInitializer();
					Expression initializer2 = declaration1.getInitializer();
					
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
									System.out.println(initializer2 + " should be replaced with " + initializer1);
									failure = true;
								}
							}
							else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
								if (!checkVariables(initializer1.toString(), initializer2.toString(), replacement)) {
									System.out.println(initializer2 + " should be replaced with " + replacement);
									failure = true;
								}
							}
							else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
								if (!checkInfixAndVariable((InfixExpression)initializer2, initializer1.toString())) {
									System.out.println(initializer2 + " should be replaced with " + initializer1);
									failure = true;
								}
							}
						}
						else if (initializer1.getNodeType() == Type.INFIX_EXPRESSION) {
							if (initializer2.getNodeType() == Type.NUMBER_LITERAL) {
								if (!checkInfixAndNumber((InfixExpression)initializer1, initializer2)) {
									System.out.println(initializer2 + " should be replaced with " + initializer1);
									failure = true;
								}
							}
							else if (initializer2.getNodeType() == Type.SIMPLE_NAME) {
								if (!checkInfixAndVariable((InfixExpression)initializer1, initializer2.toString())) {
									System.out.println(initializer2 + " should be replaced with " + replacement);
									failure = true;
								}
							}
							else if (initializer2.getNodeType() == Type.INFIX_EXPRESSION) {
								if (!checkInfix((InfixExpression)initializer1, (InfixExpression)initializer2)) {
									System.out.println(initializer2 + " should be replaced with " + initializer1);
									failure = true;
								}
							}
						}
					}
					
					return !failure;
				}
				
								// In functions before, we knew what statement was from what (correct or wrong) code, 
				// Here we dont, so we have and indicator that says if assignmnet or expression is from correct code
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
								System.out.println(prefixPostfixOperator + " should be replaced with ++");
								failure = true;
							}
						}
						else if (assignmentOperator.equals("-=")) {
							if (prefixPostfixOperator.equals("++")) {
								System.out.println(prefixPostfixOperator + " should be replaced with --" + assignmentOperator);
								failure = true;
							}
						}
						
						if (!checkVariables(variable1, variable2, replacement)) {
							System.out.println(variable2 + " should be replaced with " + replacement);
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
								System.out.println(assignmentOperator + " should be replaced with -=");
								failure = true;
							}
						}
						else if (assignmentOperator.equals("-=")) {
							if (prefixPostfixOperator.equals("++")) {
								System.out.println(assignmentOperator + " should be replaced with +=");
								failure = true;
							}
						}
						
						if (!checkVariables(variable1, variable2, replacement)) {
							System.out.println(variable1 + " should be replaced with " + replacement);
							failure = true;
						}
					}
						
					return !failure;
				}
				
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				@SuppressWarnings("unchecked")
				public boolean visit(MethodDeclaration node) {
					System.out.println("--- METHOD DECLARATION ---");
					SimpleName name = node.getName();
					StringBuilder sb = new StringBuilder();
					for (Modifier m : (List<Modifier>)node.modifiers()) {
						sb.append(m.getKeyword().toString());
						sb.append(' ');
					}
					System.out.println("Declaration of method '" + name.getFullyQualifiedName() + "' with modifiers: " + sb.toString());
					return true;
				}
	 
				public boolean visit(MethodInvocation node) {
					SimpleName name = node.getName();
					System.out.println("Invocation of method '" + name + "' at line " + cu.getLineNumber(name.getStartPosition()));
					return true;
				}
				
				public boolean visit(IfStatement node) {
					System.out.println("If: " + node.getExpression());
					
					return true;
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
						value = this.vars.get(expr + "");
					
					System.out.println("Returning " + value + " at line " + cu.getLineNumber(expr.getStartPosition()));
					return true;
				}
		});
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}
