package com.dubture.doctrine.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.search.ISearchEngine;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.index2.search.ISearchEngine.SearchFor;
import org.eclipse.dltk.core.index2.search.ISearchRequestor;
import org.eclipse.dltk.core.index2.search.ModelAccess;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.eclipse.php.internal.core.model.PhpModelAccess;

/**
 * 
 * Access to the doctrine model.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class DoctrineModelAccess extends PhpModelAccess {

	
	
	/**
	 * 
	 * Find the repositoryClass for a doctrine entity
	 * 
	 * @param className
	 * @param project
	 * @return
	 */
	public String getRepositoryClass(String className, IScriptProject project) {
		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(project);

		if(scope == null) {
			return null;
		}
		
		ISearchEngine engine = ModelAccess.getSearchEngine(PHPLanguageToolkit.getDefault());
		final List<String> repos = new ArrayList<String>();
		
		engine.search(IModelElement.USER_ELEMENT, null, null, 0, 0, 100, SearchFor.REFERENCES, MatchRule.PREFIX, scope, new ISearchRequestor() {
			
			@Override
			public void match(int elementType, int flags, int offset, int length,
					int nameOffset, int nameLength, String elementName,
					String metadata, String doc, String qualifier, String parent,
					ISourceModule sourceModule, boolean isReference) {
				
				repos.add(qualifier);

			}
		}, null);
		
		
		if (repos.size() == 1)
			return repos.get(0);
		
		return null;

	}
}