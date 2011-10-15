package com.dubture.doctrine.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class DoctrineCorePlugin implements BundleActivator {

	private static BundleContext context;
	
	public static String ID = "com.dubture.doctrine.core";	

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		DoctrineCorePlugin.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		DoctrineCorePlugin.context = null;
	}

}
