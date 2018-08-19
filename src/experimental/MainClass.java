package experimental;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.gen.jdt.cd.EntityType;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.common.collect.Lists;

public class MainClass 
{
	
	static 
	{
		Run.initGenerators();
	}
	

	public static void main(String[] args) {
		try {
			ITree t1 = Generators.getInstance().getTree("tests/test1.java").getRoot();
			ITree t2 = Generators.getInstance().getTree("tests/test2.java").getRoot();
			
			// printMyWay(t1, true);
			
			Matcher m = Matchers.getInstance().getMatcher(t1, t2);
			m.match();
			
			Set<Mapping> s = m.getMappingsAsSet();
			MappingStore st = m.getMappings();
			
			for (Mapping mapping : s) {
				System.out.println("Mapping: " + mapping.first.toShortString() + " - " + mapping.second.toShortString());
			}
			
			ActionGenerator g = new ActionGenerator(t1, t2, st);
			List<Action> actions = g.generate();
			
			for (Action action : actions) {
				System.out.print(action.getName());
				System.out.print(" :");
				printMyWay(action.getNode(), false);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static void printMyWay(ITree tree, boolean recursive)
	{
		for (ITree t : tree.preOrder()) {
			StringBuilder sb = new StringBuilder(new String(new char[t.getDepth()]).replace("\0", "    "));
			
			sb.append("[");
			if (t.hasLabel())
				sb.append(t.getLabel());
			else
				sb.append("?");
			sb.append("] pos: ");
			sb.append(t.getPos());
			sb.append(", endpos: ");
			sb.append(t.getEndPos());
			sb.append(", size: ");
			sb.append(t.getSize());
			sb.append(", type: ");
			sb.append(t.getType());
			
			sb.append(" | metadata: [");
			for (Iterator<Entry<String, Object>> it = t.getMetadata(); it.hasNext(); ) {
				Entry<String, Object> entry = it.next();
				sb.append(entry.getKey());
				sb.append(":");
				sb.append(entry.getValue().toString());
				sb.append(" ");
			}
			sb.append("]");

			
			System.out.println(sb.toString());
			
			if (!recursive)
				return;
		}
	}
}
