package com.dubture.doctrine.internal.core.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.ISourceReference;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.core.compiler.ast.nodes.IPHPDocAwareDeclaration;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.log.Logger;

public class AnnotationModuleDeclaration implements IAnnotationModuleDeclaration {
	private static AnnotationBlock EMPTY = new AnnotationBlock(new ArrayList<Annotation>(0));
	
	private Map<Integer, AnnotationBlock> annotations = new LinkedHashMap<Integer, AnnotationBlock>();
	
	public void addBlock(int start, AnnotationBlock block) {
		annotations.put(start, block);
	}
	
	public AnnotationBlock read(int position) {
		if (annotations.containsKey(position)) {
			return annotations.get(position);
		}
		
		return EMPTY;
	}
	
	@Override
	public AnnotationBlock readAnnotations(IPHPDocAwareDeclaration astNode) {
		return read(((ASTNode)astNode).sourceStart());
	}

	@Override
	public AnnotationBlock readAnnotations(ASTNode node) {
		return read(node.start());
	}

	@Override
	public AnnotationBlock readAnnotations(ISourceReference ref) {
		try {
			if (ref instanceof IField) {
				return read(ref.getNameRange().getOffset());
			}
			return read(ref.getSourceRange().getOffset());
		} catch (ModelException e) {
			Logger.logException(e);
		}
		return EMPTY;
	}

	@Override
	public AnnotationBlock[] getBlocks() {
		return annotations.values().toArray(new AnnotationBlock[annotations.size()]);
	}

	@Override
	public AnnotationBlock readAnnotations(int offset, int length) {
		return read(offset);
	}
}
