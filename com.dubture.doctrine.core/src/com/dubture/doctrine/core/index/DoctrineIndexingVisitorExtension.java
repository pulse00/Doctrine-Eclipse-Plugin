package com.dubture.doctrine.core.index;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.IIndexingRequestor.DeclarationInfo;
import org.eclipse.dltk.core.index2.IIndexingRequestor.ReferenceInfo;
import org.eclipse.php.core.index.PhpIndexingVisitorExtension;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.compiler.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.AnnotationClass;
import com.dubture.doctrine.annotation.model.AnnotationVisitor;
import com.dubture.doctrine.annotation.model.ArgumentValueType;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.core.AnnotationParserUtil;
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
@SuppressWarnings("restriction")
public class DoctrineIndexingVisitorExtension extends PhpIndexingVisitorExtension {

	private NamespaceDeclaration namespace;
	private IAnnotationModuleDeclaration decl;
	private HashMap<String, String> parts;

	@Override
	public void setSourceModule(ISourceModule module) {
		super.setSourceModule(module);
		parts = new HashMap<String, String>();
		try {
			decl = AnnotationParserUtil.getModule(module);
		} catch (CoreException e) {
			Logger.logException(e);
		}

		this.sourceModule = module;
		indexPendingEntities();

	}

	protected void indexPendingEntities() {
		List<Entity> pending = DoctrineBuilder.getPendingEntities();

		for (Entity entity : pending) {

			ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY, 0, 0, entity.getElementName(), null, entity.getFullyQualifiedName());

			requestor.addReference(entityInfo);
		}
	}
	
	

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) throws Exception {
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
						parts.put(part.getAlias() == null ? part.getNamespace().getName().toLowerCase() : part.getAlias().getName().toLowerCase(), fullName);

					}
					return false;
				}
			});
		}

		return true;
	}

	protected void processClassDeclaration(ClassDeclaration classDeclaration, DeclarationInfo info) {
		if (decl == null) {
			return;
		}
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

		ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY, classDeclaration.sourceStart(), classDeclaration.sourceEnd(),
				classDeclaration.getName(), null, qualifier);

		Logger.debugMSG("indexing entity: " + classDeclaration.getName() + " => " + qualifier);
		requestor.addReference(entityInfo);

		IArgumentValue repoArgumentValue = annotation.getArgumentValue("repositoryClass");
		if (repoArgumentValue == null || repoArgumentValue.getType() != ArgumentValueType.STRING) {
			return;
		}

		String repositoryClass = (String) repoArgumentValue.getValue();
		ReferenceInfo repositoryInfo = new ReferenceInfo(IDoctrineModelElement.REPOSITORY_CLASS, classDeclaration.sourceStart(), classDeclaration.sourceEnd(),
				classDeclaration.getName(), repositoryClass, qualifier);
		Logger.debugMSG("indexing repository class: " + classDeclaration.getName() + " => " + repositoryClass);

		requestor.addReference(repositoryInfo);
	}

	@Override
	public void modifyDeclaration(ASTNode node, DeclarationInfo info) {
	    	if (node == null) {
	    	    return;
	    	}
		if (node instanceof ClassDeclaration) {
			processClassDeclaration((ClassDeclaration) node, info);
		}
		indexReferences(node);
		super.modifyDeclaration(node, info);
	}

	private void indexReferences(ASTNode node) {
		if (decl == null) {
			return;
		}

		AnnotationBlock block = decl.readAnnotations(node);
		if (block != null) {
			block.traverse(new ReferenceAnnotationVisitor());
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
					fullName = DoctrineCoreConstants.DEFAULT_ANNOTATION_NAMESPACE  + NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName();
				}
			} else {
				if (parts.containsKey(node.getFirstNamespacePart().toLowerCase())) {
					fullName = parts.get(node.getFirstNamespacePart().toLowerCase()) + NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName();
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
			requestor.addReference(new ReferenceInfo(IModelElement.TYPE, node.getSourcePosition().startOffset + 1, node.getSourcePosition().length -1,
					name, null, qualifier));
			return super.visit(node);
		}
	}

}
