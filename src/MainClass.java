import java.io.IOException;

import com.github.gumtreediff.client.*;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;;

public class MainClass {

	public static void main(String[] args) {
		Run.initGenerators();
		String file = "tests/test1.cpp";
		TreeContext tc;
		try {
			tc = Generators.getInstance().getTree(file);
			ITree t = tc.getRoot();
			System.out.println(t.toTreeString());
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
