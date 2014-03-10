package com.dubture.doctrine.core.goals.evaluator;

import java.util.List;

import org.eclipse.dltk.core.INamespace;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ti.GoalState;
import org.eclipse.dltk.ti.ISourceModuleContext;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.dltk.ti.types.IEvaluatedType;
import org.eclipse.php.internal.core.typeinference.IModelAccessCache;
import org.eclipse.php.internal.core.typeinference.PHPClassType;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.core.typeinference.context.IModelCacheContext;

import com.dubture.doctrine.core.goals.RepositoryTypeGoal;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.DoctrineModelAccess;
import com.dubture.doctrine.core.model.Entity;
import com.dubture.doctrine.core.model.IDoctrineModelElement;

public class RepositoryGoalEvaluator extends GoalEvaluator {

	private RepositoryTypeGoal goal;
	private IEvaluatedType result;

	public RepositoryGoalEvaluator(RepositoryTypeGoal goal) {
		super(goal);
		this.goal = goal;

	}

	@Override
	public IGoal[] init() {
		if (!(goal.getContext() instanceof ISourceModuleContext)) {
			return IGoal.NO_GOALS;
		}
		ISourceModuleContext context = (ISourceModuleContext) goal.getContext();
		ISourceModule sourceModule = context.getSourceModule();
		IModelAccessCache cache = null;
		if (context instanceof IModelCacheContext) {
			cache = ((IModelCacheContext) context).getCache();
		}


		DoctrineModelAccess model = DoctrineModelAccess.getDefault();
		IScriptProject project = sourceModule.getScriptProject();

		String repo = null;
		if (!goal.getEtityName().contains(":")) {
			try {
				IType[] types = PHPModelUtils.getTypes(goal.getEtityName(), sourceModule,0, cache, null);
				if (types != null && types.length > 0) {
					model.getRepositoryClass(goal.getEtityName(), null, project);
					if (repo != null) {
						result = new PHPClassType(repo);
						return IGoal.NO_GOALS;
					}
				}
			} catch (ModelException e) {
			}
		}

		IType type = model.getExtensionType(goal.getEtityName(), project);
		if (type == null) {
			return IGoal.NO_GOALS;
		}


		// This can provide bootleneck on huge projects
		repo = model.getRepositoryClass(type.getElementName(), type.getFullyQualifiedName("\\"), project);

		if (repo == null) {
			// FIXME If force EntityRepostory, PDT hang for 15s
			result = new PHPClassType("Doctrine\\Common\\Persistence", "ObjectRepository");
			return IGoal.NO_GOALS;
		}

		try {
			IType[] types = PHPModelUtils.getTypes(repo, context.getSourceModule(), 0, cache, null);
			if (types.length > 0) {
				this.result = new PHPClassType(types[0].getFullyQualifiedName("\\"));
				return IGoal.NO_GOALS;
			}
		} catch (ModelException e1) {
			Logger.logException(e1);
		}
		IType extensionType = model.getExtensionType(repo, project);
		if (extensionType != null) {
			this.result = new PHPClassType(extensionType.getFullyQualifiedName("\\"));
		}

		return IGoal.NO_GOALS;

		///= PHPModelUtils.getTypes(repoType.getTypeQualifiedName("\\"), context.getSourceModule(), 0, cache, null);
		//return IGoal.NO_GOALS;
	}

	@Override
	public IGoal[] subGoalDone(IGoal subgoal, Object result, GoalState state) {

		return IGoal.NO_GOALS;
	}

	@Override
	public Object produceResult() {
		return result;
	}

}
