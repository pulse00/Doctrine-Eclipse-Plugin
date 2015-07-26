package com.dubture.doctrine.internal.core.validation;

import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
import org.eclipse.dltk.compiler.problem.IProblemIdentifierFactory;

public class DoctrineProblemIdentifierFactory implements IProblemIdentifierFactory {

	@Override
	public IProblemIdentifier valueOf(String localName) throws IllegalArgumentException {
		return DoctrineProblemIdentifier.valueOf(localName);
	}

	@Override
	public IProblemIdentifier[] values() {
		return DoctrineProblemIdentifier.values();
	}

}
