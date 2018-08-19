package bbe;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.*;


@SuppressWarnings("unchecked")
public class CustomASTVisitor extends ASTVisitor
{
	private HashMap<String, Integer> vars;

	
	public CustomASTVisitor()
	{
		this.vars = new HashMap<String, Integer>();
	}
	
	
	public boolean visit(VariableDeclarationStatement node) {
		Type type = node.getType();
		for (Modifier modifier : (List<Modifier>)node.modifiers()) {
			// TODO check modifiers for each variable if they match in both files
		}
		return true;
	}
	
	public boolean visit(VariableDeclarationFragment node) {
		SimpleName name = node.getName();
		// TODO add variable to the map, with respect to the renames
		return false;
	}

	public boolean visit(SimpleName node) {
		if (this.vars.containsKey(node.getIdentifier()))
			; // TODO usage of the variable
		return true;
	}
	
	// Add more functions, check experimental/JDTTest
	
}
