package com.dubture.doctrine.internal.core.validation;

import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.compiler.problem.IProblemIdentifierExtension;
import org.eclipse.dltk.compiler.problem.IProblemIdentifierExtension3;
import org.eclipse.dltk.compiler.problem.ProblemCategory;

import com.dubture.doctrine.core.DoctrineCorePlugin;

public enum DoctrineProblemIdentifier
		implements IProblemIdentifier, IProblemIdentifierExtension, IProblemIdentifierExtension3 {
	UNRESOVABLE(ProblemCategory.IMPORT);
	private IProblemCategory category;

	public static final String MARKER_TYPE = "com.dubture.doctrine.core.problem"; //$NON-NLS-1$

	private DoctrineProblemIdentifier(IProblemCategory category) {
		this.category = category;
	}

	@Override
	public String contributor() {
		return DoctrineCorePlugin.ID;
	}

	@Override
	public boolean belongsTo(IProblemCategory category) {
		return category == this.category;
	}

	@Override
	public String getMarkerType() {
		return MARKER_TYPE;
	}
}
