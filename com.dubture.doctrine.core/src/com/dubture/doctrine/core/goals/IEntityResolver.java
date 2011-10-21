package com.dubture.doctrine.core.goals;

import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;

public interface IEntityResolver {
	
	
	IType resolve(String entity, IScriptProject project);

}
