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

	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		DoctrineCorePlugin.context = bundleContext;
		plugin = this;

	}

	public void stop(BundleContext bundleContext) throws Exception {
		DoctrineCorePlugin.context = null;
		plugin = null;
		super.stop(bundleContext);
	}

	private static final String isDebugMode = "com.dubture.symfony.core/debug";//$NON-NLS-1$

	public static boolean debug() {

		String debugOption = Platform.getDebugOption(isDebugMode);
		return getDefault().isDebugging() && "true".equalsIgnoreCase(debugOption); //$NON-NLS-1$

	}

	private static DoctrineCorePlugin getDefault() {

		return plugin;
	}

}
