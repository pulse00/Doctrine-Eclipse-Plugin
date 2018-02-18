package com.dubture.doctrine.internal.core.compiler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.internal.core.ExternalSourceModule;
import org.eclipse.php.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.IPHPDocAwareDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.PHPFieldDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.PHPMethodDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.TraitDeclaration;
import org.eclipse.php.core.compiler.ast.visitor.PHPASTVisitor;

import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.core.DoctrineCorePlugin;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.utils.AnnotationUtils;

public class AnnotationParser {
	private class ASTVisitor extends PHPASTVisitor {
		private ISourceModule sourceModule;
		private AnnotationModuleDeclaration ad;
		private AnnotationCommentParser parser;
		private boolean inType = false;
		private String content;

		public ASTVisitor(ISourceModule sourceModule, AnnotationModuleDeclaration ad) {
			this.sourceModule = sourceModule;
			this.ad = ad;
			parser = AnnotationUtils.createParser();
		}

		@Override
		public boolean visit(PHPMethodDeclaration s) throws Exception {
			if (!inType) {
				return false;
			}
			parse(s);

			return false;
		}

		public boolean visit(PHPFieldDeclaration s) throws Exception {
			if (!inType) {
				return false;
			}
			parse(s);
			return false;
		};

		@Override
		public boolean visit(TraitDeclaration s) throws Exception {
			inType = true;
			parse(s);
			return super.visit(s);
		}
		
		@Override
		public boolean visit(InterfaceDeclaration s) throws Exception {
			return false;
		}

		@Override
		public boolean visit(ClassDeclaration s) throws Exception {
			inType = true;
			parse(s);
			return true;
		}
		@Override
		public boolean endvisit(ClassDeclaration s) throws Exception {
			inType = false;
			return false;
		}
		
		@Override
		public boolean endvisit(TraitDeclaration s) throws Exception {
			inType = false;
			return false;
		}

		private void parse(IPHPDocAwareDeclaration node) throws Exception {
			if (content == null) {
				if (sourceModule.getBuffer() != null) {
					content = new String(sourceModule.getBuffer().getCharacters());
				} else {
					content = sourceModule.getSource();
				}
			}
			ad.addBlock(((ASTNode) node).sourceStart(), AnnotationUtils.extractAnnotations(parser, node, content));
		}

	}

	public AnnotationParser() {
	}

	public IAnnotationModuleDeclaration parse(ISourceModule sourceModule, IModuleDeclaration moduleDeclaration, IProblemReporter reporter)
			throws CoreException {
		if (sourceModule instanceof ExternalSourceModule) {
			return null;
		}
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
}
