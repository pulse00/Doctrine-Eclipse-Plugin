package com.dubture.doctrine.core;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.prefs.BackingStoreException;

import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;

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
		new Job("Doctrine index change detector") { //$NON-NLS-1$
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IEclipsePreferences node = InstanceScope.INSTANCE.getNode(DoctrineCorePlugin.ID);
				if (!DoctrineCoreConstants.INDEX_VERSION.equals(node.get(DoctrineCoreConstants.INDEX_VERSION_PREFERENCE, null))) {
					try {
						for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
							if (project.isAccessible() && project.hasNature(DoctrineNature.NATURE_ID)) {
								monitor.subTask("Rebuild doctrine index: " + project.getName()); //$NON-NLS-1$
								project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
							}
						}
						node.put(DoctrineCoreConstants.INDEX_VERSION_PREFERENCE, DoctrineCoreConstants.INDEX_VERSION);
						node.flush();
					} catch (BackingStoreException e) {
						Logger.logException(e);
					} catch (CoreException e) {
						Logger.logException(e);
					}
				}
				
				return Status.OK_STATUS;
			}
		}.schedule(100);

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
