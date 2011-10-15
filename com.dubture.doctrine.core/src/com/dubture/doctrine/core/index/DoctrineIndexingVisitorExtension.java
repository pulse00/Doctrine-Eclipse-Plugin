package com.dubture.doctrine.core.index;

import java.io.BufferedReader;
import java.io.StringReader;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.php.core.index.PhpIndexingVisitorExtension;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTag;

import com.dubture.symfony.annotation.parser.antlr.AnnotationCommonTree;
import com.dubture.symfony.annotation.parser.antlr.AnnotationCommonTreeAdaptor;
import com.dubture.symfony.annotation.parser.antlr.AnnotationLexer;
import com.dubture.symfony.annotation.parser.antlr.AnnotationNodeVisitor;
import com.dubture.symfony.annotation.parser.antlr.AnnotationParser;
import com.dubture.symfony.annotation.parser.antlr.error.IAnnotationErrorReporter;
@SuppressWarnings("restriction")
public class DoctrineIndexingVisitorExtension extends
		PhpIndexingVisitorExtension {

	public DoctrineIndexingVisitorExtension() {

	}
	
	@Override
	public void setSourceModule(ISourceModule module) {

		super.setSourceModule(module);

		

	}
	
	
	
	@Override
	public boolean visit(TypeDeclaration s) throws Exception {

		if (s instanceof ClassDeclaration) {
			
			ClassDeclaration clazz = (ClassDeclaration) s;		
			parseClassDocBlock(clazz);
			
		}
		
		
		return true;
	}
	
	
	protected void parseClassDocBlock(ClassDeclaration clazz) {

		PHPDocBlock phpDoc = clazz.getPHPDoc();
		
		if (phpDoc == null)
			return;
		
		
		BufferedReader buffer = new BufferedReader(new StringReader(phpDoc.getLongDescription()));

		try {

			String line;

			while((line = buffer.readLine()) != null) {
				
				int start = line.indexOf('@');
				int end = line.length()-1;				
				
				String annotation = line.substring(start, end+1);
//				int sStart = sourceStart-line.toCharArray().length+line.indexOf('@');
				CharStream content = new ANTLRStringStream(annotation);
	
									
//				AnnotationErrorReporter reporter = new AnnotationErrorReporter(context, sStart);
				IAnnotationErrorReporter reporter = new IAnnotationErrorReporter() {
					
					@Override
					public void reportError(String header, String message,
							RecognitionException e) {
						// TODO Auto-generated method stub
						
					}
				};
				
				
				AnnotationLexer lexer = new AnnotationLexer(content, reporter);
				AnnotationParser parser = new AnnotationParser(new CommonTokenStream(lexer), reporter);
				parser.setTreeAdaptor(new AnnotationCommonTreeAdaptor());
				AnnotationParser.annotation_return root = parser.annotation();
				AnnotationCommonTree tree = (AnnotationCommonTree) root.getTree();
				AnnotationNodeVisitor visitor = new AnnotationNodeVisitor();
				tree.accept(visitor);
	
				String className = visitor.getClassName();
				
				if ("Entity".equals(className)) {

					System.err.println(visitor.getArgument("repositoryClass"));
					
					
				}


			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		for (PHPDocTag tag : phpDoc.getTags()) {
			
			System.err.println(tag.getValue());
			
			System.err.println(tag.getValue());
			
		}

		
	}

	

}
