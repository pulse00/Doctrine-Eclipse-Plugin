package com.dubture.doctrine.core.goals;

import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;

/**
 * 
 * 
 * Interface to allow extenders to resolve entity types to a 
 * fully qualified name, ie return the type for the aliase 'SomeBundle:User'
 * to the fully qualified type 'Some\Bundle\User'
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public interface IEntityResolver {
	
	
	/**
	 * 
	 * @param entity the entity string to resolve
	 * @param project the containing {@link IScriptProject}
	 * @return the referenced {@link IType} or null
	 */
	IType resolve(String entity, IScriptProject project);

}
