/*******************************************************************************
 * This file is part of the doctrine eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.search;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceReference;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.index2.search.ISearchEngine;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.index2.search.ISearchEngine.SearchFor;
import org.eclipse.dltk.core.index2.search.ModelAccess;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.ui.search.ElementQuerySpecification;
import org.eclipse.dltk.ui.search.IMatchPresentation;
import org.eclipse.dltk.ui.search.IQueryParticipant;
import org.eclipse.dltk.ui.search.ISearchRequestor;
import org.eclipse.dltk.ui.search.QuerySpecification;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.search.ui.text.Match;

import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.AnnotationVisitor;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.ui.log.Logger;

/**
 * @author Dawid Pakuła (zulus)
 */
public class AnnotationSearchParticipant implements IQueryParticipant {

	@Override
	public void search(final ISearchRequestor requestor, final QuerySpecification querySpecification,
			IProgressMonitor monitor) throws CoreException {
		final HashSet<ISourceModule> list = new HashSet<ISourceModule>();
		if (querySpecification.getLimitTo() != IDLTKSearchConstants.REFERENCES
				&& querySpecification.getLimitTo() != IDLTKSearchConstants.ALL_OCCURRENCES)
			return;
		if (querySpecification instanceof ElementQuerySpecification) {
			final IModelElement searchElement = ((ElementQuerySpecification) querySpecification).getElement();
			if (!(searchElement instanceof IType)) {
				return;
			}
			String fFullName = PHPModelUtils.getFullName((IType)searchElement);
			int i = fFullName.lastIndexOf(NamespaceReference.NAMESPACE_SEPARATOR);
			ISearchEngine engine = ModelAccess.getSearchEngine(PHPLanguageToolkit.getDefault());
			String qualifier = null;
			String name = fFullName;
			if (i != -1) {
				qualifier = name.substring(0, i);
				name = name.substring(i + 1);
			} 
			monitor.subTask("Search references"); //$NON-NLS-1$
			engine.search(IModelElement.TYPE, qualifier, name, 0, 0, 0, SearchFor.REFERENCES, MatchRule.EXACT, querySpecification.getScope(), new org.eclipse.dltk.core.index2.search.ISearchRequestor() {
				@Override
				public void match(int elementType, int flags, int offset, int length, int nameOffset, int nameLength, String elementName, String metadata, String doc,
						String qualifier, String parent, ISourceModule sourceModule, boolean isReference) {
					list.add(sourceModule); 
				}
			}, new SubProgressMonitor(monitor, 100));
			
			monitor.subTask("Detect annotations");
			for (final ISourceModule module : list) {
				final ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(module);
				if (moduleDeclaration == null) {
					continue;
				}
				final IAnnotationModuleDeclaration mod = AnnotationParserUtil.getModule(module);
				if (mod == null) {
					continue;
				}
				module.accept(new IModelElementVisitor() {
					
					@Override
					public boolean visit(final IModelElement element) {
						if (element instanceof ISourceReference) {
							AnnotationBlock annotations = mod.readAnnotations((ISourceReference)element);
							annotations.traverse(new AnnotationVisitor() {
								public boolean visit(com.dubture.doctrine.annotation.model.AnnotationClass node) {
									try {
										IModelElement[] codeSelect = module.codeSelect(node.getSourcePosition().startOffset+1, node.getSourcePosition().length-1);
										for (IModelElement selected : codeSelect) {
											if (selected.equals(searchElement)) {
												requestor.reportMatch(new Match(element, node.getSourcePosition().startOffset, node.getSourcePosition().length));
											}
										}
									} catch (ModelException e) {
										Logger.logException(e);
									}
									return true;
								};
							});
						}
						return true;
					}
				});
			}
		}

	}

	@Override
	public int estimateTicks(QuerySpecification specification) {
		return 100;
	}

	@Override
	public IMatchPresentation getUIParticipant() {
		return null;
	}

}
