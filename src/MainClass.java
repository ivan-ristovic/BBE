import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.common.collect.Lists;

public class MainClass {
	
	static {
		Run.initGenerators();
	}
	

	public static void main(String[] args) {
		try {
			TreeContext tctx1 = Generators.getInstance().getTree("tests/test1.cpp");
			TreeContext tctx2 = Generators.getInstance().getTree("tests/test2.cpp");
			ITree t1 = tctx1.getRoot();
			ITree t2 = tctx2.getRoot();
			
			/*
			System.out.println(t1.toTreeString());
			System.out.println(t2.toStaticHashString());
			
			fuck this
			*/
			
			printMyWay(t1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static void printMyWay(ITree t)
	{
		StringBuilder sb = new StringBuilder(new String(new char[t.getDepth()]).replace("\0", "    "));
		
		sb.append("[");
		if (t.hasLabel())
			sb.append(t.getLabel());
		else
			sb.append("no label");
		sb.append("] pos: ");
		sb.append(t.getPos());

		sb.append(" metadata: [ ");
		for (Iterator<Entry<String, Object>> it = t.getMetadata(); it.hasNext(); ) {
			Entry<String, Object> entry = it.next();
			sb.append(entry.getKey());
			sb.append(":");
			sb.append(entry.getValue().toString());
			sb.append(" ");
		}
		sb.append("]");
		
		System.out.println(sb.toString());
		
		for (ITree child : t.getChildren()) 
			printMyWay(child);
	}
}
