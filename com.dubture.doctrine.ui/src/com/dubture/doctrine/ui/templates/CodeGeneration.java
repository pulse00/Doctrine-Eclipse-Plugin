package com.dubture.doctrine.ui.templates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;

/**
 * 
 * CodeGeneration helper.
 * 
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public class CodeGeneration {
		
	public static String getGetterComment(IScriptProject sp,
			String declaringTypeName, String methodName, String fieldName,
			String fieldType, String bareFieldName, String lineDelimiter)
			throws CoreException {
		return StubUtility.getGetterComment(sp, declaringTypeName, methodName,
				fieldName, fieldType, bareFieldName, lineDelimiter);
	}

}
