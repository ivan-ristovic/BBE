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
		int depth = getBlockDepth(node);
		int order = _counters.get(depth) != null ? _counters.get(depth) : 0;
		
		return ROOT_BLOCK_ID >>> depth + order;
	}
	
	public static void incrementBlockCount(Block node) 
	{
		int depth = getBlockDepth(node);
		int order = _counters.get(depth) != null ? _counters.get(depth) : 0;
		
		Logger.logInfo("Incrementing block counter on depth: " + depth + " | new value: " + (order + 1));
		_counters.put(depth, order + 1);
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
