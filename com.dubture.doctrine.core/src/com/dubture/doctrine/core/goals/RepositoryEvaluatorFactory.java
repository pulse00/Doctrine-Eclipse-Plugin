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
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.context.MethodContext;

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
						String et = ((Scalar) children.get(0)).getValue().replace("'", "").replace("\"", "");
						if (et == null) {
							return null;
						}

						DoctrineModelAccess model = DoctrineModelAccess.getDefault();
						IScriptProject project = context.getSourceModule().getScriptProject();
						List<Entity> entities = model.getEntities(project);

						for (Entity e : entities) {
							if (et.equals(e.getFullyQualifiedName())) {
								String qualifier = null;
								INamespace ns = e.getNamespace();
								if (ns != null) {
									qualifier = ns.getQualifiedName("\\");
								}

								String repo = model.getRepositoryClass(e.getElementName(), qualifier, project);
								IDLTKSearchScope scope = SearchEngine.createSearchScope(project);
								Collection<IType> types = context.getCache().getTypes(context.getSourceModule(), repo, null, null);

								if (types.size() == 1) {
									IType type = types.iterator().next();
									return new RepositoryGoalEvaluator(goal, type);
								}
							}
						}

						IType type = model.getExtensionType(et, project);

						if (type == null) {
							return null;
						}
						// This can provide bootleneck on huge projects

						String repo = model.getRepositoryClass(type.getElementName(), type.getTypeQualifiedName("\\"), project);
						IDLTKSearchScope scope = SearchEngine.createSearchScope(project);
						Collection<IType> types = context.getCache().getTypes(context.getSourceModule(), repo, null, null);

						if (types.size() == 1) {
							IType ttype = types.iterator().next();
							return new RepositoryGoalEvaluator(goal, ttype);
						}

						IType repoType = model.getExtensionType(repo, project);

						if (repoType == null) {
							return null;
						}
						types = context.getCache().getTypes(context.getSourceModule(), repoType.getTypeQualifiedName("\\"), null, null);

						if (types.size() == 1) {
							IType ttype = types.iterator().next();
							return new RepositoryGoalEvaluator(goal, ttype);
						}
					}
				} catch (Exception e) {
					Logger.logException(e);
				}
			}
		}/*
		 * else if (goalClass == PHPDocMethodReturnTypeGoal.class) {
		 * PHPDocMethodReturnTypeGoal returnTypeGoal =
		 * (PHPDocMethodReturnTypeGoal) goal; if
		 * (!"getRepository".equals(returnTypeGoal.getMethodName())) { return
		 * null; }
		 *
		 * return new RepositoryGoalEvaluator(goal); }
		 */

		return null;
	}
}