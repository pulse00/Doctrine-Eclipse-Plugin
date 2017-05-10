package com.dubture.doctrine.ui.templates;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.templates.ScriptTemplateContextType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.ui.editor.templates.PHPTemplateContext;

@SuppressWarnings("restriction")
public class DoctrineTemplateContext extends PHPTemplateContext {

	public DoctrineTemplateContext(
			ScriptTemplateContextType phpTemplateContextType,
			IDocument document, int offset, int length,
			ISourceModule sourceModule) {
		super(phpTemplateContextType, document, offset, length, sourceModule);

	}

}
