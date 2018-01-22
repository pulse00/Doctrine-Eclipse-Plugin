package com.dubture.doctrine.internal.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.dltk.compiler.problem.ProblemSeverity;
import org.eclipse.php.internal.core.PHPCorePlugin;

import com.dubture.doctrine.internal.core.validation.DoctrineProblemIdentifier;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		PHPCorePlugin.getDefault().getProblemPreferences().setSeverity(DoctrineProblemIdentifier.UNRESOVABLE, ProblemSeverity.ERROR, DefaultScope.INSTANCE);
	}

}
