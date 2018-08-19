package bbe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.utils.Pair;

public class ParallelASTTraverser 
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
	ArrayList<Pair<String, String>> renames;
	
	
	public ParallelASTTraverser(String srcPath, String destPath) throws IOException
	{
		this.srcUnit = createCompilationUnit(readSource(srcPath));
		this.destUnit = createCompilationUnit(readSource(destPath));
		MappingFactory mf = new MappingFactory(srcPath, destPath);
		this.renames = mf.getUpdates();
	}
	
	
	// TODO this method should return an indicator value, still thinking what it should be
	public void traverse()
	{
		// TODO need to make this work in parallel
		this.srcUnit.accept(new CustomASTVisitor());
		this.destUnit.accept(new CustomASTVisitor());
	}
}
