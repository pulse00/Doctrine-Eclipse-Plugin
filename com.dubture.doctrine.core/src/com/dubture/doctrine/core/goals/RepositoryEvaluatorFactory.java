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
import org.eclipse.dltk.ti.goals.MethodReturnTypeGoal;
import org.eclipse.php.core.compiler.ast.nodes.PHPCallExpression;
import org.eclipse.php.core.compiler.ast.nodes.Scalar;
import org.eclipse.php.internal.core.compiler.ast.parser.ASTUtils;
import org.eclipse.php.internal.core.model.PHPModelAccess;
import org.eclipse.php.internal.core.typeinference.IModelAccessCache;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.core.typeinference.context.MethodContext;
import org.eclipse.php.internal.core.typeinference.goals.MethodElementReturnTypeGoal;
import org.eclipse.php.internal.core.typeinference.goals.phpdoc.PHPDocMethodReturnTypeGoal;

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

		if (goalClass == PHPDocMethodReturnTypeGoal.class) {
			PHPDocMethodReturnTypeGoal g = (PHPDocMethodReturnTypeGoal) goal;
			if (g.getMethodName().equals("getRepository") && g.getArgNames() != null && g.getArgNames().length > 0 && g.getArgNames()[0] != null) {

				return new RepositoryExpressionGoalEvaluator(goal, ASTUtils.stripQuotes(g.getArgNames()[0]));

			}
		} else if (goalClass == RepositoryTypeGoal.class) {
			return new RepositoryGoalEvaluator((RepositoryTypeGoal) goal);
		}

		return null;
	}
}