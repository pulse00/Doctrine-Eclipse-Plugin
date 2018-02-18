package com.dubture.doctrine.internal.core.compiler;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.compiler.IElementRequestor.TypeInfo;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.internal.core.ExternalSourceModule;
import org.eclipse.php.core.compiler.PHPSourceElementRequestorExtension;
import org.eclipse.php.core.compiler.ast.nodes.IPHPDocAwareDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.PHPDocTag.TagKind;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.ArgumentValueType;
import com.dubture.doctrine.annotation.model.ArrayValue;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.DoctrineFlags;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.compiler.IDoctrineModifiers;
import com.dubture.doctrine.core.log.Logger;

public class DoctrineSourceElementRequestor extends PHPSourceElementRequestorExtension {
	private final static String ANNOTATION_TAG = "Annotation"; //$NON-NLS-1$
	private final static String TARGET_TAG = "Target"; //$NON-NLS-1$
	private final static String TARGET_METHOD = "METHOD"; //$NON-NLS-1$
	private final static String TARGET_FIELD = "PROPERTY"; //$NON-NLS-1$
	private final static String TARGET_CLASS = "CLASS"; //$NON-NLS-1$
	private final static String TARGET_ANNOTATION = "ANNOTATION"; //$NON-NLS-1$
	private final static String TARGET_ALL = "ALL"; //$NON-NLS-1$

	private boolean enabled = false;
	private IAnnotationModuleDeclaration decl;
	private boolean declarationInitialized = false;

	private IAnnotationModuleDeclaration getAnnotationDeclaration() {
		if (!declarationInitialized) {
			declarationInitialized = true;
			try {
				decl = AnnotationParserUtil.getModule((ISourceModule) getSourceModule().getModelElement());
			} catch (CoreException e) {
				Logger.logException(e);
			}
		}

		return decl;
	}

	public DoctrineSourceElementRequestor() {
	}

	@Override
	public void setSourceModule(IModuleSource sourceModule) {
		super.setSourceModule(sourceModule);
		IScriptProject scriptProject = getSourceModule().getModelElement().getScriptProject();
		try {
			if (!(sourceModule instanceof ExternalSourceModule) && scriptProject.exists()
					&& scriptProject.getProject().hasNature(DoctrineNature.NATURE_ID)) {
				enabled = true;
			} else {
				enabled = false;
				declarationInitialized = true;
			}
		} catch (CoreException e) {
			Logger.logException(e);
		}
	}

	@Override
	public boolean visit(TypeDeclaration s) throws Exception {
		return super.visit(s);
	}

	@Override
	public void modifyClassInfo(TypeDeclaration typeDeclaration, TypeInfo ti) {
		if (!enabled || !DoctrineFlags.isClass(ti.modifiers)) {
			return;
		}
		checkAnnotation(typeDeclaration, ti);
	}

	private void checkAnnotation(TypeDeclaration typeDeclaration, TypeInfo ti) {
		if (typeDeclaration instanceof IPHPDocAwareDeclaration
				&& ((IPHPDocAwareDeclaration) typeDeclaration).getPHPDoc() != null
				&& ((IPHPDocAwareDeclaration) typeDeclaration).getPHPDoc().getTags(TagKind.UNKNOWN).length > 0) {
			List<Annotation> annotations = getAnnotationDeclaration().readAnnotations(typeDeclaration).getAnnotations();
			ti.modifiers = prepareAnnotationFlags(ti.modifiers, annotations);
			typeDeclaration.setModifiers(ti.modifiers);
		}
	}

	@SuppressWarnings("unchecked")
	public static int prepareAnnotationFlags(int flags, List<Annotation> annotations) {
		Annotation target = null;
		boolean isAnnotation = false;
		for (Annotation el : annotations) {
			if (el.getFirstNamespacePart() == null && el.getAnnotationClass().getClassName().equals(ANNOTATION_TAG)) {
				flags |= DoctrineFlags.AccAnnotation;
				isAnnotation = true;
			} else if (el.getFirstNamespacePart() == null
					&& el.getAnnotationClass().getClassName().equals(TARGET_TAG)) {
				target = el;
			}
		}
		if (!isAnnotation) {
			return flags;
		}
		if (target == null) {
			flags |= IDoctrineModifiers.AccTargetField | IDoctrineModifiers.AccTargetClass
					| IDoctrineModifiers.AccTargetMethod | IDoctrineModifiers.AccTargetAnnotation;

		} else if (target.hasArgument(0) && target.getArgumentValue(0).getType() == ArgumentValueType.ARRAY) {
			ArrayValue val = (ArrayValue) target.getArgumentValue(0);
			for (IArgumentValue pos : (List<IArgumentValue>) val.getValue()) {
				if (TARGET_ALL.equals(pos.getValue())) {
					flags |= IDoctrineModifiers.AccTargetField | IDoctrineModifiers.AccTargetClass
							| IDoctrineModifiers.AccTargetMethod | IDoctrineModifiers.AccTargetAnnotation;
				} else if (TARGET_ANNOTATION.equals(pos.getValue())) {
					flags |= IDoctrineModifiers.AccTargetAnnotation;
				} else if (TARGET_FIELD.equals(pos.getValue())) {
					flags |= IDoctrineModifiers.AccTargetField;
				} else if (TARGET_METHOD.equals(pos.getValue())) {
					flags |= IDoctrineModifiers.AccTargetMethod;
				} else if (TARGET_CLASS.equals(pos.getValue())) {
					flags |= IDoctrineModifiers.AccTargetClass;
				}
			}
		} else if (target.hasArgument(0) && target.getArgumentValue(0).getType() == ArgumentValueType.STRING) {
			Object value = target.getArgumentValue(0).getValue();
			if (TARGET_ALL.equals(value)) {
				flags |= IDoctrineModifiers.AccTargetField | IDoctrineModifiers.AccTargetClass
						| IDoctrineModifiers.AccTargetMethod | IDoctrineModifiers.AccTargetAnnotation;
			} else if (TARGET_ANNOTATION.equals(value)) {
				flags |= IDoctrineModifiers.AccTargetAnnotation;
			} else if (TARGET_FIELD.equals(value)) {
				flags |= IDoctrineModifiers.AccTargetField;
			} else if (TARGET_METHOD.equals(value)) {
				flags |= IDoctrineModifiers.AccTargetMethod;
			} else if (TARGET_CLASS.equals(value)) {
				flags |= IDoctrineModifiers.AccTargetClass;
			}
		} else {
			flags |= IDoctrineModifiers.AccTargetField | IDoctrineModifiers.AccTargetClass
					| IDoctrineModifiers.AccTargetMethod | IDoctrineModifiers.AccTargetAnnotation;
		}

		return flags;
	}

}
