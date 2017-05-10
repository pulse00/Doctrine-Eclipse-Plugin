package com.dubture.doctrine.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.index2.search.ISearchEngine;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.index2.search.ISearchEngine.SearchFor;
import org.eclipse.dltk.core.index2.search.ISearchRequestor;
import org.eclipse.dltk.core.index2.search.ModelAccess;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.core.util.LRUCache;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.eclipse.php.internal.core.model.PHPModelAccess;

import com.dubture.doctrine.core.goals.IEntityResolver;
import com.dubture.doctrine.core.index.ICleanListener;
import com.dubture.doctrine.core.log.Logger;

/**
 *
 * Access to the doctrine model.
 *
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class DoctrineModelAccess extends PHPModelAccess implements ICleanListener {

	private static final String ENTITYRESOLVER_ID = "com.dubture.doctrine.core.entityResolvers";

	private static DoctrineModelAccess modelInstance = null;
	private List<IEntityResolver> resolvers = null;

	private LRUCache entityCache = new LRUCache(10);
	private final String NULL_RESULT = "__NOT_FOUND__";

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
		if (className == null ) {
			return null;
		}

		String key = project.getElementName() + "/" + className;

		IDLTKSearchScope scope = SearchEngine.createSearchScope(project);

		if (scope == null) {
			return null;
		}

		ISearchEngine engine = ModelAccess.getSearchEngine(PHPLanguageToolkit.getDefault());
		final List<String> repos = new ArrayList<String>();

		engine.search(IDoctrineModelElement.REPOSITORY_CLASS, qualifier, className, 0, 0, 100, SearchFor.REFERENCES,
				MatchRule.EXACT, scope, new ISearchRequestor() {

					public void match(int elementType, int flags, int offset, int length, int nameOffset,
							int nameLength, String elementName, String metadata, String doc, String qualifier,
							String parent, ISourceModule sourceModule, boolean isReference) {
						if (metadata != null) {
							repos.add(metadata);
						}

					}
				}, null);

		if (repos.size() == 1) {
			String repo = repos.get(0);
			return repo;
		}

		return null;

	}

	public List<Entity> getEntities(IScriptProject project) {

		IDLTKSearchScope scope = SearchEngine.createSearchScope(project);

		if (scope == null) {
			return null;
		}

		ISearchEngine engine = ModelAccess.getSearchEngine(PHPLanguageToolkit.getDefault());
		final List<Entity> entities = new ArrayList<Entity>();

		engine.search(IDoctrineModelElement.ENTITY, null, null, 0, 0, 100, SearchFor.REFERENCES, MatchRule.PREFIX,
				scope, new ISearchRequestor() {

					public void match(int elementType, int flags, int offset, int length, int nameOffset,
							int nameLength, String elementName, String metadata, String doc, String qualifier,
							String parent, ISourceModule sourceModule, boolean isReference) {

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

	/**
	 * Resolve a classname via registered entityResolver extensions.
	 *
	 * @param classname
	 * @param project
	 * @return
	 */
	public IType getExtensionType(String classname, IScriptProject project) {

		String key = classname + project.getElementName();
		Object object = entityCache.get(key);
		if (object != null && object.equals(NULL_RESULT)) {
			return null;
		} else if (object instanceof IType) {
			return (IType) object;
		}


		for (IEntityResolver resolver : getResolvers()) {

			IType type = resolver.resolve(classname, project);
			// first extension wins
			if (type != null) {
				entityCache.put(key, type);
				return type;
			}
		}
		entityCache.put(classname, NULL_RESULT);

		return null;

	}

	private List<IEntityResolver> getResolvers() {

		if (resolvers != null) {
			return resolvers;
		}

		resolvers = new ArrayList<IEntityResolver>();
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(ENTITYRESOLVER_ID);

		try {
			for (IConfigurationElement element : config) {
				final Object extension = element.createExecutableExtension("class");
				if (extension instanceof IEntityResolver) {
					resolvers.add((IEntityResolver) extension);
				}
			}

		} catch (Exception e1) {
			Logger.logException(e1);
		}

		return resolvers;

	}

	public void clean() {
		entityCache.flush();
	}
}