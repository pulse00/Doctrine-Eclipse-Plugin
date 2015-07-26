package com.dubture.doctrine.internal.core.build;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.compiler.problem.ProblemCollector;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
import org.eclipse.dltk.core.builder.AbstractBuildParticipantType;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;
import org.eclipse.dltk.core.builder.IBuildParticipantFactory;
import org.eclipse.dltk.core.builder.IScriptBuilder;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPModuleDeclaration;

import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.internal.core.compiler.AnnotationParser;

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
			ISourceModuleInfo cacheEntry = SourceParserUtil.getCache().get(context.getSourceModule());
			IAnnotationModuleDeclaration annotationModule;
			if (context.getBuildType() != IScriptBuilder.FULL_BUILD) {
				annotationModule = AnnotationParserUtil.getModuleFromCache(cacheEntry, context.getProblemReporter());
				if (annotationModule != null) {
					context.set(IAnnotationModuleDeclaration.class.getName(), annotationModule);
					return;
				}
			}

			PHPModuleDeclaration module = (PHPModuleDeclaration) context.get(IBuildContext.ATTR_MODULE_DECLARATION);
			final ProblemCollector problemCollector = new ProblemCollector();
			AnnotationParser parser = new AnnotationParser();
			annotationModule = parser.parse(context.getSourceModule(), module, problemCollector);

			// put result to the cache
			AnnotationParserUtil.putModuleToCache(cacheEntry, annotationModule, problemCollector);
			// report errors to the build context
			problemCollector.copyTo(context.getProblemReporter());

			context.set(IAnnotationModuleDeclaration.class.getName(), annotationModule);
		}

	}

}
