package com.dubture.doctrine.ui.templates;

import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class DoctrineTemplateVariables {
	
	public static class ClassContainer extends TemplateVariableResolver {
		
		public static final String NAME = "class_container"; //$NON-NLS-1$

		public ClassContainer() {
			super(NAME, "Enclosing class name");
		}		
	}

}
