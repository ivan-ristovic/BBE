import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

public class JDTTest {

	public static void main(String[] args) {
		Scanner sc = null;
		try {
			sc = new Scanner(new File("tests/test1.java"));
			String src = sc.useDelimiter("\\A").next();

			ASTParser parser = ASTParser.newParser(AST.JLS8); 
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(src.toCharArray());
			parser.setResolveBindings(true);
			
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			 
			cu.accept(new ASTVisitor() {
	 
				Set<String> names = new HashSet<String>();
	 
				public boolean visit(VariableDeclarationFragment node) {
					SimpleName name = node.getName();
					this.names.add(name.getIdentifier());
					System.out.println("Declaration of '" + name + "' at line" + cu.getLineNumber(name.getStartPosition()));
					return false;
				}
	 
				public boolean visit(SimpleName node) {
					if (this.names.contains(node.getIdentifier())) {
					System.out.println("Usage of '" + node + "' at line " +	cu.getLineNumber(node.getStartPosition()));
					}
					return true;
				}
	 
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}
}
