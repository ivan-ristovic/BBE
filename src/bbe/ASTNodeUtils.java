package bbe;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

public class ASTNodeUtils 
{
	public static int getBlockDepth(ASTNode node)
	{
		if (node == null)
			return 0;
		
		if (node instanceof Block)
			return 1 + getBlockDepth(node.getParent());
		
		return getBlockDepth(node.getParent());
	}
}
