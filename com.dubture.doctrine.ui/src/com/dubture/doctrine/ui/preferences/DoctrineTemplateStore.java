package com.dubture.doctrine.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.php.internal.ui.preferences.PHPTemplateStore;

/**
 *   
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class DoctrineTemplateStore extends PHPTemplateStore {

	public DoctrineTemplateStore(ContextTypeRegistry registry,
			IPreferenceStore store, String key) {
		super(registry, store, key);

	}
}
