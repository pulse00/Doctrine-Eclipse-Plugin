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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.dubture.doctrine.core.DoctrineNature;

public class RemoveDoctrineSupportHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		if (selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();

			if (item instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) item;
				IProject project = ((IResource) adaptable.getAdapter(IResource.class)).getProject();
				if (project != null && project.exists()) {
					try {
						IProjectDescription description = project.getDescription();
						String[] natures = description.getNatureIds();
						for (int i = 0; i < natures.length; ++i) {
							if (DoctrineNature.NATURE_ID.equals(natures[i])) {
								// Remove the nature
								String[] newNatures = new String[natures.length - 1];
								System.arraycopy(natures, 0, newNatures, 0, i);
								System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
								description.setNatureIds(newNatures);
								project.setDescription(description, null);
								project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
								project.build(IncrementalProjectBuilder.FULL_BUILD, null);
								break;
							}
						}

					} catch (CoreException e) {
						throw new ExecutionException(e.getMessage(), e);
					}
				}

			}
		}
		return null;
	}

}
