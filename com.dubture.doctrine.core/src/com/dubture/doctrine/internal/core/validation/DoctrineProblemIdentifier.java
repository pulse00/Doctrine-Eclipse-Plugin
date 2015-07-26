package com.dubture.doctrine.internal.core.validation;

import org.eclipse.dltk.compiler.problem.IProblemCategory;
import org.eclipse.dltk.compiler.problem.ProblemCategory;
import org.pdtextensions.semanticanalysis.validation.IValidatorIdentifier;
import org.pdtextensions.semanticanalysis.validation.Problem;

import com.dubture.doctrine.core.DoctrineCorePlugin;
import com.dubture.doctrine.internal.core.validation.validator.AnnotationValidator;

public enum DoctrineProblemIdentifier implements IValidatorIdentifier {
	UNRESOVABLE("use", Problem.CAT_IMPORT, AnnotationValidator.ID); //$NON-NLS-1$
	private String type;
	private String validator;
	private int category;

	public static final String MARKER_TYPE = "org.pdtextensions.semanticanalysis.problem"; //$NON-NLS-1$

	private DoctrineProblemIdentifier(String type, int category, String validator) {
		this.type = type;
		this.validator = validator;
		this.category = category;
	}

	@Override
	public String type() {
		return type;
	}

	@Override
	public String validator() {
		return validator;
	}

	@Override
	public String contributor() {
		return DoctrineCorePlugin.ID;
	}

	@Override
	public boolean belongsTo(IProblemCategory category) {
		if (category == ProblemCategory.IMPORT && this.category == Problem.CAT_IMPORT) {
			return true;
		}

		return false;
	}

	@Override
	public String getMarkerType() {
		return MARKER_TYPE;
	}

	@Override
	public int getCategory() {
		return category;
	}

}
