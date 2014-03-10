package com.dubture.doctrine.core.goals;

import org.eclipse.dltk.ti.IContext;
import org.eclipse.dltk.ti.goals.AbstractGoal;

public class RepositoryTypeGoal extends AbstractGoal {

	private String entityName;

	/**
	 * @param context
	 * @param entityName
	 */
	public RepositoryTypeGoal(IContext context, String entityName) {
		super(context);
		assert entityName != null;
		this.entityName = entityName.length() > 2 ? entityName.replaceAll("\\\\\\\\", "\\\\") : entityName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		} else if(!(obj instanceof  RepositoryTypeGoal)) {
			return false;
		}

		return this.entityName.equals(((RepositoryTypeGoal)obj).getEtityName());
	}

	public String getEtityName() {
		return entityName;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * this.entityName.hashCode();
	}

}
