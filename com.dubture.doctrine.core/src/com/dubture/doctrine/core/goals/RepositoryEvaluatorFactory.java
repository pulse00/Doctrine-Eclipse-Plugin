package com.dubture.doctrine.core.goals;

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


	

	@SuppressWarnings("unchecked")
	@Override
	public GoalEvaluator createEvaluator(IGoal goal) {

		Class<?>goalClass = goal.getClass();

		if (!(goal.getContext() instanceof MethodContext)) {
			return null;
		}

		MethodContext context = (MethodContext) goal.getContext();

		if (goalClass == ExpressionTypeGoal.class) {

			ExpressionTypeGoal expGoal = (ExpressionTypeGoal) goal;			
			ASTNode expression = expGoal.getExpression();
			
			if (expression instanceof PHPCallExpression) {
				
				PHPCallExpression expr = (PHPCallExpression) expression;
				
				// are we calling a method named "get" ?
				if (expr.getName().equals("getRepository")) {
				
					try {
						
						CallArgumentsList args = expr.getArgs();
						List<Object> children = args.getChilds();
						
						if (children.size() > 0) {
							
							if (args.getChilds().get(0) instanceof Scalar) {
						
								Scalar entity = (Scalar) children.get(0);
								String et = entity.getValue().replace("'", "").replace("\"", "");
								
								if (et == null)
									return null;
								
								DoctrineModelAccess model = DoctrineModelAccess.getDefault();
								
								IScriptProject project = context.getSourceModule().getScriptProject();
								List<Entity> entities = model.getEntities(project);
								PhpModelAccess phpmodel = PhpModelAccess.getDefault();
								
								
								for (Entity e : entities) {
									

									if (et.equals(e.getFullyQualifiedName())) {
									
										String qualifier = null;										
										INamespace ns = e.getNamespace();
										
										if (ns != null) {											
											qualifier = ns.getQualifiedName("\\");
										}
										
										String repo = model.getRepositoryClass(e.getElementName(), qualifier, project);
										IDLTKSearchScope scope = SearchEngine.createSearchScope(project);
										IType[] types = phpmodel.findTypes(repo, MatchRule.EXACT, 0, 0, scope, null);
										
										if (types.length == 1) {											
											IType type = types[0];
											return new RepositoryGoalEvaluator(goal, type);
										}
									}
								}
								
								IType type = model.getExtensionType(et, project);
								
								if (type == null)
									return null;
								
								String repo = model.getRepositoryClass(type.getElementName(), type.getTypeQualifiedName("\\"), project);
								
								IDLTKSearchScope scope = SearchEngine.createSearchScope(project);
								IType[] types = phpmodel.findTypes(repo, MatchRule.EXACT, 0, 0, scope, null);
								
								if (types.length == 1) {											
									IType ttype = types[0];
									return new RepositoryGoalEvaluator(goal, ttype);
								}
								
								IType repoType = model.getExtensionType(repo, project);
								
								if (repoType == null)
									return null;

								types = phpmodel.findTypes(repoType.getTypeQualifiedName("\\"), MatchRule.EXACT, 0, 0, scope, null);
								
								if (types.length == 1) {											
									IType ttype = types[0];
									return new RepositoryGoalEvaluator(goal, ttype);
								}
							}							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}				
			}
		}
		return null;
	}
}