package experimental;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.*;

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
						case "<":
							System.out.println("analizaaaa");
					}
					
					// Some dummy default return value, will never come here
					return 0;
				}
				
				public boolean visit(Block node) {
					System.out.println("--- BLOCK ---");
					
					// perform var checks here
					
					return true;
				}
	 
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


