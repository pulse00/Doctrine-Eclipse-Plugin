package com.dubture.doctrine.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.ui.DoctrineUIPlugin;

public class RemoveDoctrineSupportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		if (selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();

			if (item instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) item;
				final IProject project = ((IResource) adaptable.getAdapter(IResource.class)).getProject();
				if (project != null && project.exists()) {
					new Job("Remove Doctrine Support") {
						
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								SubMonitor sub = SubMonitor.convert(monitor, 5);
								IProjectDescription description = project.getDescription();
								String[] natures = description.getNatureIds();
								sub.worked(1);
								for (int i = 0; i < natures.length; ++i) {
									if (DoctrineNature.NATURE_ID.equals(natures[i])) {
										// Remove the nature
										String[] newNatures = new String[natures.length - 1];
										System.arraycopy(natures, 0, newNatures, 0, i);
										System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
										description.setNatureIds(newNatures);
										sub.worked(1);
										project.setDescription(description, sub.split(1));
										if (!monitor.isCanceled()) {
											project.build(IncrementalProjectBuilder.CLEAN_BUILD, sub.split(1));
										}
										if (!monitor.isCanceled()) {
											project.build(IncrementalProjectBuilder.FULL_BUILD, sub.split(1));
										}
										if (!monitor.isCanceled()) {
											sub.done();
										}
										break;
									}
								}

							} catch (CoreException e) {
								new Status(IStatus.ERROR, DoctrineUIPlugin.PLUGIN_ID, e.getMessage(), e);
				            }
							if (monitor.isCanceled()) {
								return Status.CANCEL_STATUS;
							}
							return Status.OK_STATUS;
						}
					}.schedule();
				}

			}
		}
		return null;
	}

}
