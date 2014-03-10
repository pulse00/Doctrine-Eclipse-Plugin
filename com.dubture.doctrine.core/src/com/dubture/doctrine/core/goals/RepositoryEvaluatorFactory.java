package com.dubture.doctrine.core.goals;

import java.util.Collection;
import java.util.List;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.expressions.CallArgumentsList;
import org.eclipse.dltk.core.INamespace;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.ti.IGoalEvaluatorFactory;
import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPCallExpression;
import org.eclipse.php.internal.core.compiler.ast.nodes.Scalar;
import org.eclipse.php.internal.core.compiler.ast.parser.ASTUtils;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.IModelAccessCache;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.core.typeinference.context.MethodContext;

import com.dubture.doctrine.core.goals.evaluator.RepositoryExpressionGoalEvaluator;
import com.dubture.doctrine.core.goals.evaluator.RepositoryGoalEvaluator;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.DoctrineModelAccess;
import com.dubture.doctrine.core.model.Entity;

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

		if (!(goal.getContext() instanceof MethodContext)) {
			return null;
		}

		MethodContext context = (MethodContext) goal.getContext();

		if (goalClass == ExpressionTypeGoal.class) {

			ExpressionTypeGoal expGoal = (ExpressionTypeGoal) goal;
			ASTNode expression = expGoal.getExpression();

			if (!(expression instanceof PHPCallExpression)) {
				return null;
			}
			PHPCallExpression expr = (PHPCallExpression) expression;

			// are we calling a method named "get" ?
			if (expr.getName().equals("getRepository") && expr.getArgs().getChilds().size() > 0) {

				try {

					final List<?> children = expr.getArgs().getChilds();
					if (children.get(0) instanceof Scalar) {
						String et = ASTUtils.stripQuotes(((Scalar) children.get(0)).getValue());
						if (et == null) {
							return null;
						}

						return new RepositoryExpressionGoalEvaluator(goal, et);

					}
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
		} else if (goalClass == RepositoryTypeGoal.class) {
			return new RepositoryGoalEvaluator((RepositoryTypeGoal) goal);
		}			
			/*
		 * else if (goalClass == PHPDocMethodReturnTypeGoal.class) {
		 * PHPDocMethodReturnTypeGoal returnTypeGoal =
		 * (PHPDocMethodReturnTypeGoal) goal; if
		 * (!"getRepository".equals(returnTypeGoal.getMethodName())) { return
		 * null; }
		 *
		 * return new RepositoryExpressionGoalEvaluator(goal); }
		 */

		return null;
	}
}