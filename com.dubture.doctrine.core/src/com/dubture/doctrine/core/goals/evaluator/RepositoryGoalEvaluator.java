package com.dubture.doctrine.core.goals.evaluator;

import org.eclipse.dltk.core.IType;

import org.eclipse.dltk.ti.GoalState;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.php.internal.core.typeinference.PHPClassType;

/**
 * 
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class RepositoryGoalEvaluator extends GoalEvaluator {

	private IType type;
	
	public RepositoryGoalEvaluator(IGoal goal) {
		super(goal);

	}

	public RepositoryGoalEvaluator(IGoal goal, IType iType) {
		super(goal);
		
		type = iType;
		
	}

	@Override
	public IGoal[] init() {

		return null;
	}

	@Override
	public IGoal[] subGoalDone(IGoal subgoal, Object result, GoalState state) {

		return IGoal.NO_GOALS;
	}

	@Override
	public Object produceResult() {

		if (type == null)
			return null;

		String fqn = type.getFullyQualifiedName("\\");
		
		if (fqn != null)
			return new PHPClassType(fqn);
		
		return null;
	}

}
