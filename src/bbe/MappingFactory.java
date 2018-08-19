package bbe;

import java.io.IOException;
import java.util.ArrayList;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.utils.Pair;

public class MappingFactory 
{
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
			if (action.getName() == "UPD") {
				// TODO find out how to get the post-change name
				renames.add(new Pair<String, String>(action.getNode().getLabel(), action.getNode().getLabel()));
			}
		}

		return renames;
	}
}
