package com.dubture.doctrine.ui.templates;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.templates.ScriptTemplateContext;
import org.eclipse.dltk.ui.templates.ScriptTemplateContextType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.ui.editor.templates.PhpTemplateContext;

@SuppressWarnings("restriction")
public class DoctrineContextType extends ScriptTemplateContextType {

	public static final String DOCTRINE_CONTEXT_TYPE_ID = "doctrine"; //$NON-NLS-1$

	public ScriptTemplateContext createContext(IDocument document, int offset,
			int length, ISourceModule sourceModule) {
		return new PhpTemplateContext(this, document, offset, length,
				sourceModule);
	}

	@Override
	protected void addScriptResolvers() {
		
		super.addScriptResolvers();
		addResolver(new DoctrineTemplateVariables.ClassContainer());

	}
}
