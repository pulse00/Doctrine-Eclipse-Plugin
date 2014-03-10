package com.dubture.doctrine.core.goals.evaluator;

import org.eclipse.dltk.ti.GoalState;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.dltk.ti.types.IEvaluatedType;

import com.dubture.doctrine.core.goals.RepositoryTypeGoal;

/**
 * @author Robert Gruendler <r.gruendler@gmail.com>
 */
public class RepositoryExpressionGoalEvaluator extends GoalEvaluator {

	private String entityName;
	private final static int STATE_INIT = 0;
	private final static int STATE_WAITNG_TYPE = 2;
	private final static int STATE_GOT_TYPE = 3;

	private int state = STATE_INIT;
	private IEvaluatedType result;

	public RepositoryExpressionGoalEvaluator(IGoal goal) {
		super(goal);
	}

	public RepositoryExpressionGoalEvaluator(IGoal goal, String entityName) {
		super(goal);
		this.entityName = entityName;
	}

	@Override
	public IGoal[] init() {
		IGoal goal = produceNextSubgoal(null, null, null);
		if (goal != null) {
			return new IGoal[] { goal };
		}
		return IGoal.NO_GOALS;
	}

	private IGoal produceNextSubgoal(IGoal previousGoal,
			IEvaluatedType previousResult, GoalState goalState) {
		if (state == STATE_INIT) {
			state = STATE_WAITNG_TYPE;
			return new RepositoryTypeGoal(goal.getContext(), entityName);
		} else if (state == STATE_WAITNG_TYPE) {
			result = previousResult;
			state = STATE_GOT_TYPE;
		}

		return null;
	}

	@Override
	public IGoal[] subGoalDone(IGoal subgoal, Object result, GoalState state) {
		IGoal goal = produceNextSubgoal(subgoal, (IEvaluatedType) result, state);
		if (goal != null) {
			return new IGoal[] { goal };
		}
		return IGoal.NO_GOALS;
	}

	@Override
	public Object produceResult() {
		return result;
	}
}
