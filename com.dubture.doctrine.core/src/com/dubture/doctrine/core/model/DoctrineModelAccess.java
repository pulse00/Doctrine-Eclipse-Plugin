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

	
	private static DoctrineModelAccess modelInstance = null;	
	
	
	public static DoctrineModelAccess getDefault() {

		if (modelInstance == null)
			modelInstance = new DoctrineModelAccess();

		return modelInstance;
	}
	
	/**
	 * 
	 * Find the repositoryClass for a doctrine entity
	 * 
	 * @param className
	 * @param project
	 * @return
	 */
	public String getRepositoryClass(String className, String qualifier, IScriptProject project) {
		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(project);

		if(scope == null) {
			return null;
		}
		
		ISearchEngine engine = ModelAccess.getSearchEngine(PHPLanguageToolkit.getDefault());
		final List<String> repos = new ArrayList<String>();
		
		
		engine.search(IDoctrineModelElement.REPOSITORY_CLASS, null, className, 0, 0, 100, SearchFor.REFERENCES, MatchRule.EXACT, scope, new ISearchRequestor() {
			
			@Override
			public void match(int elementType, int flags, int offset, int length,
					int nameOffset, int nameLength, String elementName,
					String metadata, String doc, String qualifier, String parent,
					ISourceModule sourceModule, boolean isReference) {
				
				if (metadata != null)
					repos.add(metadata);

			}
		}, null);
		
		
		if (repos.size() == 1) {
			return repos.get(0);
		}
		
		return null;

	}
	
	public List<Entity> getEntities(IScriptProject project) {

		IDLTKSearchScope scope = SearchEngine.createSearchScope(project);

		if(scope == null) {
			return null;
		}
		
		ISearchEngine engine = ModelAccess.getSearchEngine(PHPLanguageToolkit.getDefault());
		final List<Entity> entities = new ArrayList<Entity>();
		
		engine.search(IDoctrineModelElement.ENTITY, null, null, 0, 0, 100, SearchFor.REFERENCES, MatchRule.PREFIX, scope, new ISearchRequestor() {
			
			@Override
			public void match(int elementType, int flags, int offset, int length,
					int nameOffset, int nameLength, String elementName,
					String metadata, String doc, String qualifier, String parent,
					ISourceModule sourceModule, boolean isReference) {
				
				String name = "";
				
				if (qualifier != null)
					name = qualifier + "\\";
					
				name += elementName;
				Entity e = new Entity(null, name);
				
				entities.add(e);

			}
		}, null);
		
		
		return entities;
		
	}
}