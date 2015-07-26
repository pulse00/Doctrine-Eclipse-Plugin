package com.dubture.doctrine.internal.core.compiler;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.compiler.problem.IProblem;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.IPHPDocAwareDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPFieldDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPMethodDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPModuleDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.TraitDeclaration;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.core.DoctrineCorePlugin;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.utils.AnnotationUtils;

public class AnnotationParser extends PHPASTVisitor {
	private class ASTVisitor extends PHPASTVisitor {
		private ISourceModule sourceModule;
		private AnnotationModuleDeclaration ad;
		private AnnotationCommentParser parser;

		public ASTVisitor(ISourceModule sourceModule, AnnotationModuleDeclaration ad) {
			this.sourceModule = sourceModule;
			this.ad = ad;
			parser = AnnotationUtils.createParser();
		}

		@Override
		public boolean visit(PHPMethodDeclaration s) throws Exception {
			parse(s);

			return super.visit(s);
		}

		public boolean visit(PHPFieldDeclaration s) throws Exception {
			parse(s);
			return super.visit(s);
		};

		@Override
		public boolean visit(TraitDeclaration s) throws Exception {
			parse(s);
			return super.visit(s);
		}

		@Override
		public boolean visit(ClassDeclaration s) throws Exception {
			parse(s);
			return super.visit(s);
		}

		private void parse(IPHPDocAwareDeclaration node) throws Exception {
			ad.addBlock(((ASTNode) node).sourceStart(), AnnotationUtils.extractAnnotations(parser, node, sourceModule));
		}

	}

	public AnnotationParser() {
	}

	public IAnnotationModuleDeclaration parse(ISourceModule sourceModule, IModuleDeclaration moduleDeclaration, IProblemReporter reporter)
			throws CoreException {
		AnnotationModuleDeclaration decl = new AnnotationModuleDeclaration();

		ASTVisitor astVisitor = new ASTVisitor(sourceModule, decl);
		if (moduleDeclaration instanceof ModuleDeclaration) {
			try {
				((ModuleDeclaration) moduleDeclaration).traverse(astVisitor);
			} catch (CoreException e) {
				throw e;
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, DoctrineCorePlugin.ID, "Problem during parse", e)); //$NON-NLS-1$
			}
		}

		return decl;
	}

	public IAnnotationModuleDeclaration parse(ISourceModule sourceModule, IProblemReporter reporter) throws CoreException {
		return parse(sourceModule, SourceParserUtil.getModuleDeclaration(sourceModule, reporter), reporter);
	}

}
