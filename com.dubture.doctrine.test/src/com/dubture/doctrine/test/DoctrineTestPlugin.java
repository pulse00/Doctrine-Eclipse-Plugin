package com.dubture.doctrine.test;

import org.eclipse.php.core.tests.PHPCoreTests;
import org.osgi.framework.BundleContext;

public class DoctrineTestPlugin extends PHPCoreTests {

	private static BundleContext context;

	public static String ID = "com.dubture.doctrine.core";

	static BundleContext getContext() {
		return context;
	}

	private static DoctrineTestPlugin plugin;

	public void start(BundleContext bundleContext) throws Exception {
		DoctrineTestPlugin.context = bundleContext;
		plugin = this;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		DoctrineTestPlugin.context = null;
		plugin = null;
	}


	public static DoctrineTestPlugin getDefault() {
		return plugin;
	}

}
