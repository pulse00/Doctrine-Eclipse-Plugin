package com.dubture.doctrine.internal.core.validation.factory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;
import org.pdtextensions.semanticanalysis.validation.IValidatorFactory;
import org.pdtextensions.semanticanalysis.validation.IValidatorParticipant;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.internal.core.validation.validator.AnnotationValidator;

public class AnnotationValidatorFactory implements IValidatorFactory {

	@Override
	public IValidatorParticipant getValidatorParticipant(IScriptProject scriptProject) {
		try {
			if (scriptProject.getProject().hasNature(DoctrineNature.NATURE_ID)) {
				return new AnnotationValidator();
			}
		} catch (CoreException e) {
			Logger.logException(e);
		}
		return null;
	}

}
