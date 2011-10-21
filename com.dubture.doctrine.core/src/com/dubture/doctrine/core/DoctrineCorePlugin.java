package com.dubture.doctrine.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class DoctrineCorePlugin extends Plugin {

	private static BundleContext context;
	
	public static String ID = "com.dubture.doctrine.core";	

	static BundleContext getContext() {
		return context;
	}

	private static DoctrineCorePlugin plugin;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		DoctrineCorePlugin.context = bundleContext;
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		DoctrineCorePlugin.context = null;
		plugin = null;
	}
	
	private static final String isDebugMode = "com.dubture.symfony.core/debug";

	public static boolean debug() {
		
		String debugOption = Platform.getDebugOption(isDebugMode); //$NON-NLS-1$
		return getDefault().isDebugging() && "true".equalsIgnoreCase(debugOption); 
		
	}

	private static DoctrineCorePlugin getDefault() {

		return plugin;
	}
	

}
