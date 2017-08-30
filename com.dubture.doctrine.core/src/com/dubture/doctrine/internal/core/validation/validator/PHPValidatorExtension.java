package com.dubture.doctrine.internal.core.validation.validator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.php.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.PHPModuleDeclaration;
import org.eclipse.php.core.compiler.ast.validator.IValidatorExtension;
import org.eclipse.php.core.compiler.ast.validator.IValidatorVisitor;
import org.eclipse.php.core.compiler.ast.validator.IValidatorVisitor.IUsePartInfo;

import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.AnnotationClass;
import com.dubture.doctrine.annotation.model.AnnotationVisitor;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;

public class PHPValidatorExtension implements IValidatorExtension {
	private IBuildContext context;
	private IValidatorVisitor validator;

	@Override
	public void visit(ASTNode s) throws Exception {

	}

	@Override
	public void endvisit(ASTNode s) throws Exception {
		if (s instanceof PHPModuleDeclaration) {
			if (!validator.hasNamespace()) {
				validate(s.sourceStart(), s.sourceEnd());
			}
			
		} else if (s instanceof NamespaceDeclaration) {
			validate(s.sourceStart(), s.sourceEnd());
		}
	}
	
	protected void validate(int start, int end) throws CoreException
	{
		IAnnotationModuleDeclaration module = AnnotationParserUtil.getModule(context.getSourceModule());
		AnnotationVisitor visitor = new AnnotationVisitor() {
			@Override
			public boolean visit(AnnotationClass node) {
				IUsePartInfo usePartInfo = validator.getUsePartInfo((node.getFirstNamespacePart() == null ? node.getClassName() : node.getFirstNamespacePart()).toLowerCase());
				if (usePartInfo != null) {
					usePartInfo.increaseRefCount();
				}
				return super.visit(node);
			}
		};
		for (AnnotationBlock block : module.getBlocks()) {
			if (block.getSourcePosition().startOffset >= start && block.getSourcePosition().endOffset <= end) {
				block.traverse(visitor);
			}
		}
	}

	@Override
	public void init(IBuildContext buildContext, IValidatorVisitor validator) {
		this.context = buildContext;
		this.validator = validator;
	}

	@Override
	public boolean isSupported(IBuildContext buildContext) {
		try {
			return buildContext.getSourceModule().getScriptProject().getProject().hasNature(DoctrineNature.NATURE_ID);
		} catch (CoreException e) {
			System.out.println(e);
			return false;
		}
	}

	@Override
	public boolean skipProblem(int start, int end, String message, IProblemIdentifier id) {
		return false;
	}

	@Override
	public boolean skipProblem(ASTNode node, String message, IProblemIdentifier id) {
		return false;
	}

}
