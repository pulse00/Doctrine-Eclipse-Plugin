/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 *
 * (c) Dawid Pakuła <zulus@w3des.net>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
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
import com.dubture.doctrine.core.DoctrineCorePlugin;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.log.Logger;

public class PHPValidatorExtension implements IValidatorExtension {
	private IBuildContext context;
	private IValidatorVisitor validator;
	private IAnnotationModuleDeclaration annotationModule;

	@Override
	public void visit(ASTNode s) throws Exception {

	}

	@Override
	public void endvisit(ASTNode s) throws Exception {
		if (s instanceof PHPModuleDeclaration) {
			if (!validator.hasNamespace()) {
				validate(s.sourceStart(), s.sourceEnd());
			}
			if (annotationModule != null) {
				s.traverse(new AnnotationValidatorVisitor(context, validator, annotationModule));
			}
		} else if (s instanceof NamespaceDeclaration) {
			validate(s.sourceStart(), s.sourceEnd());
		}
	}
	
	protected void validate(int start, int end) throws CoreException
	{
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
		for (AnnotationBlock block : annotationModule.getBlocks()) {
			if (block.getSourcePosition().startOffset >= start && block.getSourcePosition().endOffset <= end) {
				block.traverse(visitor);
			}
		}
	}

	@Override
	public void init(IBuildContext buildContext, IValidatorVisitor validator) {
		this.context = buildContext;
		this.validator = validator;
		try {
			annotationModule = AnnotationParserUtil.getModule(context.getSourceModule());
		} catch (CoreException e) {
			Logger.logException(e);
		}
		
	}

	@Override
	public boolean isSupported(IBuildContext buildContext) {
		try {
			return buildContext.getSourceModule().getScriptProject().getProject().hasNature(DoctrineNature.NATURE_ID);
		} catch (CoreException e) {
			Logger.logException(e);
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
