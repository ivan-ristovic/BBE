package bbe;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

public class ASTNodeUtils 
{
	public static final int ROOT_BLOCK_ID = 1 << 30;
	private static HashMap<Integer, Integer> _counters = new HashMap<>();
	
	public static int getBlockId(ASTNode node)
	{
		return getBlockId(node, false);
	}
	
	public static int getBlockId(ASTNode node, boolean add)
	{
		int depth = getBlockDepth(node);
		int order = _counters.get(depth) != null ? _counters.get(depth) : 0;
		
		if (add && node instanceof Block) {
			Logger.logInfo("incrementing block counter on depth: " + depth + " | new value: " + (order + 1));
			_counters.put(depth, order + 1);
		}
		
		Logger.logInfo("returning block id: " + (ROOT_BLOCK_ID >>> depth + order));
		return ROOT_BLOCK_ID >>> depth + order;
	}
	
	public static int getBlockDepth(ASTNode node)
	{
		if (node == null)
			return 0;
		
		if (node instanceof Block)
			return 1 + getBlockDepth(node.getParent());
		
		return getBlockDepth(node.getParent());	
	}
	
	public static void resetCounters()
	{
		_counters.clear();
	}
}
