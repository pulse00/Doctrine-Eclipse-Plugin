package com.dubture.doctrine.core.index;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.Declaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.IIndexingRequestor.DeclarationInfo;
import org.eclipse.dltk.core.index2.IIndexingRequestor.ReferenceInfo;
import org.eclipse.dltk.internal.core.ExternalSourceModule;
import org.eclipse.php.core.index.PHPIndexingVisitorExtension;
import org.eclipse.php.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.IPHPDocAwareDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.core.compiler.ast.nodes.TraitDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.core.compiler.ast.nodes.UseStatement;
import org.eclipse.php.core.compiler.ast.nodes.PHPDocTag.TagKind;
import org.eclipse.php.core.compiler.ast.visitor.PHPASTVisitor;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.AnnotationClass;
import com.dubture.doctrine.annotation.model.AnnotationVisitor;
import com.dubture.doctrine.annotation.model.ArgumentValueType;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.Entity;
import com.dubture.doctrine.core.model.IDoctrineModelElement;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;
import com.dubture.doctrine.internal.core.compiler.DoctrineSourceElementRequestor;

/**
 * Visits Doctrine Annotations.
 *
 * Currently indexes Entity classes and their corresponding repositoryClass.
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public class DoctrineIndexingVisitorExtension extends PHPIndexingVisitorExtension {

	private NamespaceDeclaration namespace;
	private IAnnotationModuleDeclaration decl;
	private HashMap<String, String> parts;
	private boolean enabled;
	private boolean declarationInitialized = false;

	private IAnnotationModuleDeclaration getAnnotationDeclaration() {
		if (!declarationInitialized) {
			declarationInitialized = true;
			try {
				decl = AnnotationParserUtil.getModule((ISourceModule) sourceModule);
			} catch (CoreException e) {
				Logger.logException(e);
			}
		}

		return decl;
	}

	@Override
	public void setSourceModule(ISourceModule module) {
		super.setSourceModule(module);
		IScriptProject scriptProject = module.getScriptProject();
		try {
			if (!(module instanceof ExternalSourceModule) && scriptProject.exists()
					&& scriptProject.getProject().hasNature(DoctrineNature.NATURE_ID)) {
				enabled = true;
			} else {
				enabled = false;
				declarationInitialized = true;
			}
		} catch (CoreException e) {
			Logger.logException(e);
		}
		if (!enabled) {
			return;
		}
		parts = new HashMap<String, String>();
		indexPendingEntities();

	}

	protected void indexPendingEntities() {
		List<Entity> pending = DoctrineBuilder.getPendingEntities();

		for (Entity entity : pending) {

			ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY, 0, 0, entity.getElementName(),
					null, entity.getFullyQualifiedName());

			requestor.addReference(entityInfo);
		}
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) throws Exception {
		if (!enabled) {
			return false;
		}
		if (typeDeclaration instanceof NamespaceDeclaration) {
			NamespaceDeclaration namespaceDeclaration = (NamespaceDeclaration) typeDeclaration;
			namespace = namespaceDeclaration;
			parts = new HashMap<String, String>();
			namespaceDeclaration.traverse(new PHPASTVisitor() {
				public boolean visit(UseStatement s) throws Exception {
					if (s.getStatementType() != UseStatement.T_NONE) {
						return false;
					}
					for (UsePart part : s.getParts()) {
						if (part.getNamespace() == null) {
							continue;
						}
						String fullName = part.getNamespace().getFullyQualifiedName();
						parts.put(part.getAlias() == null ? part.getNamespace().getName().toLowerCase()
								: part.getAlias().getName().toLowerCase(), fullName);

					}
					return false;
				}

				@Override
				public boolean visitGeneral(ASTNode node) throws Exception {
					if (node instanceof Declaration) {
						return false;
					}
					return super.visitGeneral(node);
				}
			});
		}

		return true;
	}

	protected void processClassDeclaration(ClassDeclaration classDeclaration, DeclarationInfo info) {
		List<Annotation> annotations = decl.readAnnotations((ASTNode) classDeclaration).getAnnotations();

		if (annotations.size() < 1) {
			return;
		}
		Annotation annotation = null;
		for (Annotation a : annotations) {
			if (a.getClassName().equals("Entity")) {
				annotation = a;
				break;
			}
		}

		if (annotation == null) {
			info.flags = DoctrineSourceElementRequestor.prepareAnnotationFlags(info.flags, annotations);
			return;
		}

		String qualifier = null;
		if (namespace != null) {
			qualifier = namespace.getName();
		}

		ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY, classDeclaration.sourceStart(),
				classDeclaration.sourceEnd(), classDeclaration.getName(), null, qualifier);

		requestor.addReference(entityInfo);

		IArgumentValue repoArgumentValue = annotation.getArgumentValue("repositoryClass");
		if (repoArgumentValue == null || repoArgumentValue.getType() != ArgumentValueType.STRING) {
			return;
		}

		String repositoryClass = (String) repoArgumentValue.getValue();
		ReferenceInfo repositoryInfo = new ReferenceInfo(IDoctrineModelElement.REPOSITORY_CLASS,
				classDeclaration.sourceStart(), classDeclaration.sourceEnd(), classDeclaration.getName(),
				repositoryClass, qualifier);

		requestor.addReference(repositoryInfo);
	}

	@Override
	public void modifyDeclaration(ASTNode node, DeclarationInfo info) {
		if (!enabled) {
			return;
		}
		if (node == null || node instanceof InterfaceDeclaration || node instanceof NamespaceDeclaration
				|| !(node instanceof IPHPDocAwareDeclaration)) {
			return;
		}
		PHPDocBlock block = ((IPHPDocAwareDeclaration) node).getPHPDoc();
		if (block == null || block.getTags(TagKind.UNKNOWN).length == 0 || getAnnotationDeclaration() == null) {
			return;
		}
		if (node instanceof ClassDeclaration) {
			processClassDeclaration((ClassDeclaration) node, info);
		}
		indexReferences(node);
	}

	private void indexReferences(ASTNode node) {
		AnnotationBlock annotations = decl.readAnnotations(node);
		if (annotations != null) {
			annotations.traverse(new ReferenceAnnotationVisitor());
		}
	}

	private class ReferenceAnnotationVisitor extends AnnotationVisitor {
		@Override
		public boolean visit(AnnotationClass node) {
			String fullName;
			if (!node.hasNamespace()) {
				if (parts.containsKey(node.getClassName().toLowerCase())) {
					fullName = parts.get(node.getClassName().toLowerCase());
				} else {
					if (DoctrineCoreConstants.ANNOTATION_TAG_NAME.equals(node.getClassName())) {
						return super.visit(node);
					}
					fullName = DoctrineCoreConstants.DEFAULT_ANNOTATION_NAMESPACE
							+ NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName();
				}
			} else {
				if (parts.containsKey(node.getFirstNamespacePart().toLowerCase())) {
					fullName = parts.get(node.getFirstNamespacePart().toLowerCase())
							+ NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName();
				} else {
					fullName = node.getFullyQualifiedName();
				}
			}
			int i = fullName.lastIndexOf(NamespaceReference.NAMESPACE_SEPARATOR);
			String qualifier = null;
			String name = fullName;
			if (i != -1) {
				qualifier = name.substring(0, i);
				name = name.substring(i + 1);
			}
			requestor.addReference(new ReferenceInfo(IModelElement.TYPE, node.getSourcePosition().startOffset + 1,
					node.getSourcePosition().length - 1, name, null, qualifier));
			return super.visit(node);
		}
	}

}
