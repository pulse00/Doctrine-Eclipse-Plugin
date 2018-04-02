package com.dubture.doctrine.core.goals.evaluator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.evaluation.types.AmbiguousType;
import org.eclipse.dltk.evaluation.types.MultiTypeType;
import org.eclipse.dltk.internal.core.SourceModuleElementInfo;
import org.eclipse.dltk.ti.GoalState;
import org.eclipse.dltk.ti.ISourceModuleContext;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.dltk.ti.types.IEvaluatedType;
import org.eclipse.php.internal.core.typeinference.GenericClassType;
import org.eclipse.php.internal.core.typeinference.PHPClassType;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.core.typeinference.context.INamespaceContext;
import org.eclipse.php.internal.core.typeinference.context.MethodContext;
import org.eclipse.php.internal.core.typeinference.context.TypeContext;
import org.eclipse.php.internal.core.typeinference.goals.phpdoc.PHPDocClassVariableGoal;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.annotation.model.StringValue;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.DoctrineModelAccess;
import com.dubture.doctrine.internal.core.compiler.AnnotationModuleDeclaration;

@SuppressWarnings("restriction")
public class EntityFieldEvaluator extends GoalEvaluator {

	private Object result = null;
	public EntityFieldEvaluator(PHPDocClassVariableGoal goal) {
		super(goal);
	}

	@Override
	public IGoal[] init() {
		if (!(goal.getContext() instanceof ISourceModuleContext)) {
			return IGoal.NO_GOALS;
		}
		ISourceModule sourceModule = ((ISourceModuleContext)goal.getContext()).getSourceModule();
		if (sourceModule == null) {
			return IGoal.NO_GOALS;
		}
		
		try {
			PHPDocClassVariableGoal goal = (PHPDocClassVariableGoal)getGoal();
			IAnnotationModuleDeclaration module = AnnotationParserUtil.getModule(sourceModule);
			
			TypeContext context = (TypeContext) goal.getContext();
			IType type;
			if (context.getNamespace() != null) {
				type = sourceModule.getType(context.getNamespace().charAt(0) == '\\' ? context.getNamespace().substring(1) : context.getNamespace());
				if (type == null) {
					return IGoal.NO_GOALS;
				}
				type = type.getType(PHPModelUtils.extractElementName(context.getInstanceType().getTypeName()));
			} else {
				type = sourceModule.getType(context.getInstanceType().getTypeName());
			}
			if (type == null) {
				return IGoal.NO_GOALS;
			}
			IField field = type.getField(goal.getVariableName());
			if (field == null) {
				return IGoal.NO_GOALS;
			}
			AnnotationBlock annotations = module.readAnnotations(field);
			List<Annotation> find = annotations.findAnnotations("ManyToOne");
			if (!find.isEmpty()) {
				for (Annotation  a : find) {
					String name = buildClassName(a.getArgumentValue("targetEntity"));
					if (name != null) {
						result = new PHPClassType(name);
						break;
					}
				}
			}
			find = annotations.findAnnotations("OneToMany");
			if (find.isEmpty()) {
				find = annotations.findAnnotations("ManyToMany");
			}
			if (!find.isEmpty()) {
				PHPClassType generic = new PHPClassType("\\Doctrine\\Common\\Collections\\Collection");
				MultiTypeType multi = new MultiTypeType();
				result = new AmbiguousType(new IEvaluatedType[] {generic, multi});
				for (Annotation  a : find) {
					String name = buildClassName(a.getArgumentValue("targetEntity"));
					if (name != null) {
						multi.addType(new PHPClassType(name));
						break;
					}
				}
			}
		} catch (CoreException e) {
			Logger.logException(e);
		}
		
		
		return IGoal.NO_GOALS;
	}
	
	private String buildClassName(IArgumentValue val)
	{
		if (val == null) {
			return null;
		}
		String tmp = val.toString();
		if (tmp.contains("\\")) {
			return tmp;
		} else {
			return new StringBuilder(((INamespaceContext)getGoal().getContext()).getNamespace()).append('\\').append(tmp).toString();
		}
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
