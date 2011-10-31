package com.dubture.doctrine.ui.templates;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.templates.ScriptTemplateContext;
import org.eclipse.dltk.ui.templates.ScriptTemplateContextType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.ContextTypeRegistry;

/**
 * 
 * Code templates types for Doctrine.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public class CodeTemplateContextType extends ScriptTemplateContextType {

	
	public static final String DOCTRINE_PREFIX = "doctrine_"; //$NON-NLS-1$
	public static final String GETTERSTUB_CONTEXTTYPE = DOCTRINE_PREFIX
			+ "getter_stub_context"; //$NON-NLS-1$
	
	
	private static final String CODETEMPLATES_PREFIX = "com.dubture.doctrine.codetemplates."; //$NON-NLS-1$
	public static final String COMMENT_SUFFIX = "comment"; //$NON-NLS-1$
	
	public static final String GETTER_STUB_ID = CODETEMPLATES_PREFIX
			+ "getter_stub"; //$NON-NLS-1$	
	public static final String GETTERCOMMENT_ID = CODETEMPLATES_PREFIX
			+ "getter" + COMMENT_SUFFIX; //$NON-NLS-1$
	
	public static final String FIELD_TYPE = "field_name";  //$NON-NLS-1$
	public static final String BARE_FIELD_NAME = "bare_field_name";  //$NON-NLS-1$
	

	public CodeTemplateContextType(String contextName) {
		this(contextName, contextName);

	}
	
	public CodeTemplateContextType(String contextName, String name) {
		super(contextName, name);

	}		
	
	@Override
	public ScriptTemplateContext createContext(IDocument document,
			int completionPosition, int length, ISourceModule sourceModule) {

		return new DoctrineTemplateContext(this, document, completionPosition, length, sourceModule);
	}
	
	public static void registerContextTypes(ContextTypeRegistry registry) {

		registry.addContextType(new CodeTemplateContextType(CodeTemplateContextType.GETTERSTUB_CONTEXTTYPE));

	}	

}
