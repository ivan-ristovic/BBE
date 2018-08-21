package bbe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.utils.Pair;

public class ASTTraverser 
{
	
	public static CompilationUnit createCompilationUnit(String code)
	{
		ASTParser parser = ASTParser.newParser(AST.JLS8); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(code.toCharArray());
		parser.setResolveBindings(true);
		
		return (CompilationUnit) parser.createAST(null);
	}
	
	private static String readSource(String path) throws FileNotFoundException 
	{
		Scanner sc = null;
		String content = null;
		try {
			sc = new Scanner(new File(path));
			content = sc.useDelimiter("\\A").next();
		} finally {
			if (sc != null)
				sc.close();
		}
		
		return content;
	}

	
	private CompilationUnit srcUnit;
	private CompilationUnit destUnit;
	private ArrayList<Pair<String, String>> renames;
	
	
	public ASTTraverser(String srcPath, String destPath, ArrayList<Pair<String, String>> renames) throws IOException
	{
		this.srcUnit = createCompilationUnit(readSource(srcPath));
		this.destUnit = createCompilationUnit(readSource(destPath));
		this.renames = renames;
	}
	
	
	public HashMap<Integer, HashMap<String, Integer>> traverseSrcTree()
	{
		SrcASTVisitor visitor = new SrcASTVisitor();
		this.srcUnit.accept(visitor);
		return visitor.getDeclaredVars();
	}
	
	public void traverseDestTree(HashMap<Integer, HashMap<String, Integer>> expectedVars)
	{
		DestASTVisitor visitor = new DestASTVisitor(expectedVars);
		this.destUnit.accept(visitor);
		// return indicator of success
	}
}
