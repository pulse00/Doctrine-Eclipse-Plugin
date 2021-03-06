package com.dubture.doctrine.core.compiler;


import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.core.ISourceReference;
import org.eclipse.php.core.compiler.ast.nodes.IPHPDocAwareDeclaration;

import com.dubture.doctrine.annotation.model.AnnotationBlock;


public interface IAnnotationModuleDeclaration {
	
	public AnnotationBlock readAnnotations(IPHPDocAwareDeclaration astNode);
	
	public AnnotationBlock readAnnotations(ASTNode node);
	
	public AnnotationBlock readAnnotations(ISourceReference ref);
	
	public AnnotationBlock readAnnotations(int offset, int length);
	
	public AnnotationBlock[] getBlocks();
}
