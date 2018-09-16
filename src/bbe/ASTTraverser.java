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

	
	private static CompilationUnit srcUnit;
	private static CompilationUnit destUnit;
	private ArrayList<Pair<String, String>> renames;
	
	
	public ASTTraverser(String srcPath, String destPath, ArrayList<Pair<String, String>> renames) throws IOException
	{
		srcUnit = createCompilationUnit(readSource(srcPath));
		destUnit = createCompilationUnit(readSource(destPath));
		this.renames = renames;
	}
	
	
	public HashMap<Integer, BlockVariableMap> traverseSrcTree()
	{
		SrcASTVisitor visitor = new SrcASTVisitor();
		srcUnit.accept(visitor);
		return visitor.getDeclaredVars();
	}
	
	public HashMap<Integer, BlockVariableMap> traverseDestTree(HashMap<Integer, BlockVariableMap> expectedVars)
	{
		DestASTVisitor visitor = new DestASTVisitor(expectedVars, renames);
		destUnit.accept(visitor);
		return visitor.getDeclaredVars();
	}
	
	public static void printWithLineNumber(String s, int startPos)
	{
		System.out.println(s);
		int startLineNumber = ASTTraverser.destUnit.getLineNumber(startPos);
		System.out.println("  (at line: " + startLineNumber + ")");
	}
}
