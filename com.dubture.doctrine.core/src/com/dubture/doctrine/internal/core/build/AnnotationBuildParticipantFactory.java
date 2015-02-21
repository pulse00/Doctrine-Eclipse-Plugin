package com.dubture.doctrine.internal.core.build;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
import org.eclipse.dltk.core.builder.AbstractBuildParticipantType;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;
import org.eclipse.dltk.core.builder.IBuildParticipantFactory;
import org.eclipse.dltk.internal.core.ModelManager;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPFieldDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPMethodDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPModuleDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.TraitDeclaration;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;

import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.utils.AnnotationUtils;

@SuppressWarnings("restriction")
public class AnnotationBuildParticipantFactory extends AbstractBuildParticipantType implements IBuildParticipantFactory {

	public AnnotationBuildParticipantFactory() {
	}

	@Override
	public IBuildParticipant createBuildParticipant(IScriptProject project) throws CoreException {
		if (project.getProject().hasNature(DoctrineNature.NATURE_ID)) {
			return new AnnotationBuildParticipant();
		}
		return null;
	}

	private class AnnotationBuildParticipant implements IBuildParticipant {

		@Override
		public void build(IBuildContext context) throws CoreException {
			if (context.get(IBuildContext.ATTR_MODULE_DECLARATION) == null) {
				return; // Library dir
			}
			ISourceModuleInfo cacheEntry = ModelManager.getModelManager().getSourceModuleInfoCache().get(context.getSourceModule());
			
			PHPModuleDeclaration module = (PHPModuleDeclaration) context.get(IBuildContext.ATTR_MODULE_DECLARATION);
			
			try {
				module.traverse(new PHPASTVisitor() {
					@Override
					public boolean visit(PHPMethodDeclaration s) throws Exception {
						return super.visit(s);
					}
					
					public boolean visit(PHPFieldDeclaration s) throws Exception {
						return super.visit(s);
					};
					
					@Override
					public boolean visit(TraitDeclaration s) throws Exception {
						return super.visit(s);
					}
					
					@Override
					public boolean visit(ClassDeclaration s) throws Exception {
						return super.visit(s);
					}
				});
			} catch (Exception e) {
				Logger.logException(e);
			}
		}

	}

}
