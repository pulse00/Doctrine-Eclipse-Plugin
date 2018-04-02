package com.dubture.doctrine.core.goals;

import org.eclipse.dltk.ast.references.VariableReference;
import org.eclipse.dltk.ti.IGoalEvaluatorFactory;
import org.eclipse.dltk.ti.IInstanceContext;
import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.php.internal.core.compiler.ast.parser.ASTUtils;
import org.eclipse.php.internal.core.typeinference.context.MethodContext;
import org.eclipse.php.internal.core.typeinference.goals.FactoryMethodMethodReturnTypeGoal;
import org.eclipse.php.internal.core.typeinference.goals.phpdoc.PHPDocClassVariableGoal;

import com.dubture.doctrine.core.goals.evaluator.EntityFieldEvaluator;
import com.dubture.doctrine.core.goals.evaluator.RepositoryExpressionGoalEvaluator;
import com.dubture.doctrine.core.goals.evaluator.RepositoryGoalEvaluator;

/**
 *
 * Evaluates repository classes from calls like
 *
 * <pre>
 *
 * $repo = $em->getRepository('Acme\DemoBundle\Entity\SomeEntity');
 *
 * $repo->| <-- evaluates to the repoClass of SomeEntity
 *
 * </pre>
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class RepositoryEvaluatorFactory implements IGoalEvaluatorFactory {

	public GoalEvaluator createEvaluator(IGoal goal) {

		Class<?> goalClass = goal.getClass();

		if (goalClass == PHPDocClassVariableGoal.class && goal.getContext() instanceof IInstanceContext) {
			 EntityFieldEvaluator entityFieldEvaluator = new EntityFieldEvaluator((PHPDocClassVariableGoal) goal);
			 entityFieldEvaluator.init();
			 if (entityFieldEvaluator.produceResult() != null) {
				 return entityFieldEvaluator;
			 }
		}
		if (!(goal.getContext() instanceof MethodContext)) {
			return null;
		}

		if (goalClass == FactoryMethodMethodReturnTypeGoal.class) {
			FactoryMethodMethodReturnTypeGoal g = (FactoryMethodMethodReturnTypeGoal) goal;
			if (g.getMethodName().equals("getRepository") && g.getArgNames() != null && g.getArgNames().length > 0
					&& g.getArgNames()[0] != null) {

				return new RepositoryExpressionGoalEvaluator(goal, ASTUtils.stripQuotes(g.getArgNames()[0]));

			}
		} else if (goalClass == RepositoryTypeGoal.class) {
			return new RepositoryGoalEvaluator((RepositoryTypeGoal) goal);
		}

		return null;
	}
}