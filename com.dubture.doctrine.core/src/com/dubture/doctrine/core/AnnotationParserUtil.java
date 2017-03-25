package com.dubture.doctrine.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.compiler.problem.IProblemReporter;
import org.eclipse.dltk.compiler.problem.ProblemCollector;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
import org.eclipse.php.core.compiler.ast.nodes.PHPModuleDeclaration;
import org.eclipse.dltk.core.SourceParserUtil;

import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.internal.core.compiler.AnnotationParser;

/**
 * @author zulus
 */
public class AnnotationParserUtil {
	private static final String MODULE_KEY = "_annotations"; //$NON-NLS-1$
	private static final String ERRORS_KEY = "_annotation_errors"; //$NON-NLS-1$
	public static IAnnotationModuleDeclaration getModule(ISourceModule module) throws CoreException {
		return getModule(module, null);
	}
	
	
	
	
	public static IAnnotationModuleDeclaration getModule(ISourceModule module, IProblemReporter reporter) throws CoreException {
		IAnnotationModuleDeclaration moduleDeclaration = null;
		
		final ISourceModuleInfo mifo = SourceParserUtil.getCache().get(module);
		if (mifo != null) {
			moduleDeclaration = (IAnnotationModuleDeclaration) mifo.get(MODULE_KEY);
			if (moduleDeclaration != null) {
				if (reporter != null) {
					final ProblemCollector collector = (ProblemCollector) mifo
							.get(ERRORS_KEY);
					if (collector != null) {
						collector.copyTo(reporter);
					}
				}
				return moduleDeclaration;
			}
		}
		ModuleDeclaration md = SourceParserUtil.getModuleDeclaration(module, reporter);
		if (!(md instanceof PHPModuleDeclaration)) { 
			return null;
		}
		
		AnnotationParser annotationParser = new AnnotationParser();
		try {
			ProblemCollector collector = new ProblemCollector();
			moduleDeclaration = annotationParser.parse(module, collector);
			
			putModuleToCache(mifo, moduleDeclaration, collector);
			if (reporter != null) {
				collector.copyTo(collector);
			}
		} catch (CoreException e) {
			throw e;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, DoctrineCorePlugin.ID, "Problem during parse", e)); //$NON-NLS-1$
		}
		
		return moduleDeclaration;
	}

	public static IAnnotationModuleDeclaration getModuleFromCache(ISourceModuleInfo mifo,
			IProblemReporter reporter) {
		if (mifo != null) {
			final IAnnotationModuleDeclaration moduleDeclaration = (IAnnotationModuleDeclaration) mifo
					.get(MODULE_KEY);
			if (moduleDeclaration != null && reporter != null) {
				final ProblemCollector collector = (ProblemCollector) mifo
						.get(ERRORS_KEY);
				if (collector != null) {
					collector.copyTo(reporter);
				}
			}
			return moduleDeclaration;
		}
		return null;
	}
	
	public static void putModuleToCache(ISourceModuleInfo info,
			IAnnotationModuleDeclaration module, ProblemCollector collector) {
		info.put(MODULE_KEY, module);
		if (collector != null && !collector.isEmpty()) {
			info.put(ERRORS_KEY, collector);
		} else {
			info.remove(ERRORS_KEY);
		}
	}
	
}
