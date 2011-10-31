package com.dubture.doctrine.ui;

import java.io.IOException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.dubture.doctrine.ui.preferences.DoctrineTemplateStore;
import com.dubture.doctrine.ui.preferences.PreferenceConstants;
import com.dubture.doctrine.ui.templates.CodeTemplateContextType;

/**
 * The activator class controls the plug-in life cycle
 */
public class DoctrineUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.dubture.doctrine.ui"; //$NON-NLS-1$

	// The shared instance
	private static DoctrineUIPlugin plugin;
	
	
	private DoctrineTemplateStore fCodeTemplateStore;
	
	
	protected ContextTypeRegistry codeTypeRegistry = null;
	
	/**
	 * The constructor
	 */
	public DoctrineUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DoctrineUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public TemplateStore getCodeTemplateStore() {
		if (fCodeTemplateStore == null) {

			fCodeTemplateStore = new DoctrineTemplateStore(
					getCodeTemplateContextRegistry(), getPreferenceStore(),
					PreferenceConstants.CODE_TEMPLATES_KEY);

			try {
				fCodeTemplateStore.load();
			} catch (IOException e) {
//				Logger.logException(e);
			}
		}

		return fCodeTemplateStore;
	}
	
	public ContextTypeRegistry getCodeTemplateContextRegistry() {
		if (codeTypeRegistry == null) {
			ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();

			CodeTemplateContextType.registerContextTypes(registry);

			codeTypeRegistry = registry;
		}

		return codeTypeRegistry;
	}
	
	
}
