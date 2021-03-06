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
import com.dubture.doctrine.ui.log.Logger;

public class AddDoctrineSupportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		if (selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();

			if (item instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) item;
				final IProject project = ((IResource) adaptable.getAdapter(IResource.class)).getProject();
				if (project != null && project.exists()) {

					new Job("Add doctrine support") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								SubMonitor sub = SubMonitor.convert(monitor, 5);
								IProjectDescription description = project.getDescription();
								String[] natures = description.getNatureIds();
								String[] newNatures = new String[natures.length + 1];
								System.arraycopy(natures, 0, newNatures, 0, natures.length);
								newNatures[natures.length] = DoctrineNature.NATURE_ID;
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