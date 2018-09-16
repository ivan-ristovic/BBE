package bbe;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.jrubyparser.ast.Node;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.utils.Pair;

public class MappingFactory 
{
	static 
	{
		Run.initGenerators();
	}
	static String MISSING = "MISSING";	
	
	private ITree t1;
	private ITree t2;
	private Matcher matcher;
	
		
	public MappingFactory(String src, String dest) throws IOException
	{
		this.t1 = Generators.getInstance().getTree(src).getRoot();
		this.t2 = Generators.getInstance().getTree(dest).getRoot();
		this.matcher = Matchers.getInstance().getMatcher(this.t1, this.t2);
		this.matcher.match();
	}
	
	
	public ArrayList<Pair<String, String>> getUpdates()
	{
		ArrayList<Pair<String, String>> renames = new ArrayList<Pair<String, String>>();
		
		ActionGenerator g = new ActionGenerator(t1, t2, this.matcher.getMappings());
		for (Action action : g.generate()) {
			if (action instanceof Update) {
				Update upd = (Update)action;
				Logger.logInfo("Update action: " + upd.getNode().getLabel() + ", " + upd.getValue());				
				if (upd.getNode().getType() != Type.SIMPLE_NAME || !typeIsImportant(upd.getNode()))
					continue;
				renames.add(new Pair<String, String>(upd.getNode().getLabel(), upd.getValue()));
				Logger.logInfo("Update action! Adding to list: " + upd.getNode().getLabel() + ", " + upd.getValue());
			}
			else if (action instanceof Delete){
				Logger.logInfo("Delete action: " + action.getNode().getLabel());
				Delete del = (Delete)action;
				if (del.getNode().getLabel().isEmpty())
					continue;
				// do not add pair (int, null) in the list
				if (del.getNode().getType() == Type.PRIMITIVE_TYPE || del.getNode().getType() == Type.NUMBER_LITERAL)
					continue;
				// consider only different variable names as difference
				if (!typeIsImportant(del.getNode()))
					continue;
				renames.add(new Pair<String, String>(del.getNode().getLabel(), MISSING));
				Logger.logInfo("Delete action! Adding to list: " + del.getNode().getLabel() + ", " + MISSING);
			}
			else if (action instanceof Insert) {
				Logger.logInfo("Insert action: " + action.getNode().getLabel());
				Insert ins = (Insert)action;
				if (ins.getNode().getLabel().isEmpty())
					continue;
				// do not add pair (null, int) in the list
				if (ins.getNode().getType() == Type.PRIMITIVE_TYPE || ins.getNode().getType() == Type.NUMBER_LITERAL)
					continue;
				// consider only different variable names as difference
				if (!typeIsImportant(ins.getNode()))
					continue;
				renames.add(new Pair<String, String>(MISSING, ins.getNode().getLabel()));
				Logger.logInfo("Insert action! Adding to list: " + MISSING + ", " + ins.getNode().getLabel());
			}
			else {
				Logger.logInfo("Unhandeled action! Action name" + action.getName());				
			}
		}

		return renames;
	}
	
	public boolean hasOnlyVariableUpdateActions()
	{
		ActionGenerator g = new ActionGenerator(t1, t2, this.matcher.getMappings());
		for (Action action : g.generate()) {
			if (!(action instanceof Update))
				return false;

			Update upd = (Update)action;
			if (upd.getNode().getType() != Type.SIMPLE_NAME)
				return false;
		}
		
		return true;
	}
	
	private boolean typeIsImportant(ITree node)
	{
		int type = node.getType();
		if (type == Type.VARIABLE_DECLARATION_FRAGMENT || type == Type.VARIABLE_DECLARATION_STATEMENT)
			return true;
		int parentType = node.getParent() != null ? node.getParent().getType() : 0;
		if (parentType == Type.VARIABLE_DECLARATION_FRAGMENT || parentType == Type.VARIABLE_DECLARATION_STATEMENT)
			return true;
		return false;
	}
}
