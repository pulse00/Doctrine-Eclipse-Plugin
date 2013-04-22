package com.dubture.doctrine.core.index;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import com.dubture.doctrine.core.log.Logger;

public class ResourceVisitor implements IResourceVisitor, IResourceDeltaVisitor {

	public boolean visit(IResource resource) throws CoreException {
		if (resource instanceof IFile) {
			handleResource((IFile) resource);
			return false;
		}

		return true;
	}

	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource instanceof IFile) {
			handleResource((IFile) resource);
			return false;
		}

		return true;
	}

	protected void handleResource(IFile resource) {
		try {
			if ("xml".equals(resource.getFileExtension())) {
				XmlMappingParser parser = new XmlMappingParser(resource.getContents());
				parser.parse();
			}
		} catch (Exception e) {
			Logger.logException(e);
		}
	}
}
