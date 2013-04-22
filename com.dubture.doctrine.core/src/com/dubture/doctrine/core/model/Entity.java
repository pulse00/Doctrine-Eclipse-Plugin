package com.dubture.doctrine.core.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.INamespace;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.internal.core.SourceNamespace;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.core.SourceTypeElementInfo;
import org.eclipse.dltk.internal.core.hierarchy.FakeType;

/**
 * 
 * A Doctrine2 Entity implemented as {@link SourceType}.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class Entity extends SourceType {

	public Entity(ModelElement parent, String name) {
		super(parent, name);
	}
	
	
	@Override
	public String getElementName() {
	
		if (this.name.contains("\\")) {			
			String[] parts = this.name.split("\\\\");
			
			StringBuilder builder = new StringBuilder();
			
			boolean build = false;
			for (int i=0; i < parts.length; i++) {
			    
			    if (parts[i].equals("Entity")) {
			        build = true;
			        continue;
			    }
			    
			    if (build) {
			        builder.append(parts[i]);
			        builder.append("\\");
			    }			    
			}
			
			String name = builder.toString();
			
			if (name.length() == 0) {
				return super.getElementName();
			}
			
			return name.substring(0, name.length()-1);
		}
		
		return super.getElementName();
	}

	@Override
	public int getFlags() throws ModelException {
		return Modifiers.AccPublic;
	}

	@Override
	public Object getElementInfo() throws ModelException {
		return new FakeTypeElementInfo();
	}

	@Override
	protected Object openWhenClosed(Object info, IProgressMonitor monitor)throws ModelException {
		return new FakeTypeElementInfo();
	}

	@Override
	public ISourceModule getSourceModule() {
		return super.getSourceModule();
	}

	@Override
	public IModelElement getParent() {

		// avoid showing the same name twice in each codeassist
		// popup row, ie:
		// AcmeDemoBundle - AcmeDemoBundle
		return new FakeType(parent, "");		
	}
	
	public class FakeTypeElementInfo extends SourceTypeElementInfo {
		@Override
		public String getFileName() {
			return "";
		}		
	}
	
	@Override
	public INamespace getNamespace() throws ModelException {

		if (getElementName().contains("\\")) {
			String[] parts = getElementName().split("\\\\");
			if (parts.length > 1) {
				String[] ns = new String[parts.length -1];				
				System.arraycopy(parts, 0, ns, 0, ns.length);				
				return new SourceNamespace(ns);
			}
		}
		return super.getNamespace();
	}
	
	@Override
	public String getFullyQualifiedName() {
		if (this.name.contains("\\")) {
			return this.name;
		}
		
		if (this.parent == null) {
			return "";
		}

		return super.getFullyQualifiedName();
	}
}