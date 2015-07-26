/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.internal.core.validation.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.compiler.problem.DefaultProblem;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.php.internal.core.compiler.ast.nodes.ArrayCreation;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.FullyQualifiedReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.GotoLabel;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPFieldDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPMethodDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.TraitDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.compiler.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.pdtextensions.semanticanalysis.validation.IValidatorContext;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.AnnotationClass;
import com.dubture.doctrine.annotation.model.AnnotationVisitor;
import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.annotation.parser.antlr.SourcePosition;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;
import com.dubture.doctrine.internal.core.validation.DoctrineProblemIdentifier;

/**
 *
 * {@link AnnotationValidatorVisitor} parses annotations from PHPDocBlocks.
 *
 * This will mainly be used for error reporting purposes and maybe syntax
 * highlighting.
 *
 * For code-assistance in annotations, see
 *
 * @see http://symfony.com/blog/symfony2-annotations-gets-better
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class AnnotationValidatorVisitor extends PHPASTVisitor {

	final private static String MESSAGE_CANNOT_RESOLVE_TYPE = "The annotation %s cannot be resolved";

	private ClassDeclaration currentClass = null;
	private PHPMethodDeclaration currentMethod = null;

	private IValidatorContext context;
	private ISourceModule sourceModule;
	private IAnnotationModuleDeclaration annotationModuleDeclaration;

	private Map<String, String> parts;
	private Map<String, Boolean> resolved;

	public AnnotationValidatorVisitor(IValidatorContext context, IAnnotationModuleDeclaration moduleDeclaration) {
		this.context = context;
		this.sourceModule = context.getSourceModule();
		this.annotationModuleDeclaration = moduleDeclaration;
		parts = new HashMap<String, String>();
		resolved = new HashMap<String, Boolean>();
	}

	@Override
	public boolean visit(UseStatement s) {
		if (s.getStatementType() != UseStatement.T_NONE) {
			return false;
		}
		for (UsePart part : s.getParts()) {
			if (part.getNamespace() == null) {
				continue;
			}
			String fullName = part.getNamespace().getFullyQualifiedName();
			if (part.getAlias() == null) {
				parts.put(part.getAlias() == null ? part.getNamespace().getName().toLowerCase() : part.getAlias().getName().toLowerCase(), fullName);
			}

		}

		return false;
	}
	
	@Override
	public boolean visit(NamespaceDeclaration s) throws Exception {
		parts = new HashMap<String, String>();
		return super.visit(s);
	}

	/**
	 * This could be used to parse Annotationclasses themselves to build up an
	 * internal model about the annotation.
	 *
	 * However, there's no clean way at the moment as pretty much any class can
	 * be used as an annotation and there's no proper way to detect the
	 * semantics of the annotation from the php code.
	 *
	 * @see http://www.doctrine-project.org/jira/browse/DDC-1198
	 */
	@Override
	public boolean visit(ClassDeclaration classDeclaration) throws Exception {
		checkAnnotation(classDeclaration);
		return true;
	}

	@Override
	public boolean visit(PHPFieldDeclaration s) throws Exception {
		checkAnnotation(s);
		return super.visit(s);
	}

	@Override
	public boolean visit(TraitDeclaration s) throws Exception {
		checkAnnotation(s);
		return super.visit(s);
	}

	/**
	 * Parses annotations from method declarations.
	 */
	@Override
	public boolean visit(PHPMethodDeclaration methodDeclaration) throws Exception {
		checkAnnotation(methodDeclaration);

		return true;
	}

	@Override
	public boolean visit(UsePart part) {
		return true;

	}

	private void checkAnnotation(ASTNode node) {
		AnnotationBlock annotations = annotationModuleDeclaration.readAnnotations(node);
		if (annotations.isEmpty()) {
			return;
		}
		annotations.traverse(new AnnotationVisitor() {
			@Override
			public boolean visit(AnnotationClass node) {
				String fullName;
				if (node.hasNamespace() && !parts.containsKey(node.getFirstNamespacePart().toLowerCase())) {
					context.registerProblem(DoctrineProblemIdentifier.UNRESOVABLE, String.format(MESSAGE_CANNOT_RESOLVE_TYPE, node.getFullyQualifiedName()),
							node.getSourcePosition().startOffset + 1, node.getSourcePosition().endOffset);
					return true;
				}
				if (!node.hasNamespace()) {
					if (parts.containsKey(node.getClassName().toLowerCase())) {
						fullName = parts.get(node.getClassName().toLowerCase());
					} else {
						fullName = DoctrineCoreConstants.DEFAULT_ANNOTATION_NAMESPACE  + NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName();
					}
				} else {
					fullName = parts.get(node.getFirstNamespacePart().toLowerCase());
				}
				String lower = fullName.toLowerCase();
				if (!resolved.containsKey(lower)) {
					IDLTKSearchScope searchScope = SearchEngine.createSearchScope(sourceModule.getScriptProject());
					IType[] types = PhpModelAccess.getDefault().findTypes(fullName,
							MatchRule.EXACT, 0, 0, searchScope, new NullProgressMonitor());

					
					resolved.put(lower, types.length > 0);
				}
				if (!resolved.get(lower)) {
					context.registerProblem(DoctrineProblemIdentifier.UNRESOVABLE, String.format(MESSAGE_CANNOT_RESOLVE_TYPE, node.getFullyQualifiedName()),
							node.getSourcePosition().startOffset + 1, node.getSourcePosition().endOffset);
				}
				return super.visit(node);
			}
		});
	}
}
