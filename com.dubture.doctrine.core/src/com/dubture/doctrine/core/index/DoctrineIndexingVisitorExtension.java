package com.dubture.doctrine.core.index;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.IIndexingRequestor.DeclarationInfo;
import org.eclipse.dltk.core.index2.IIndexingRequestor.ReferenceInfo;
import org.eclipse.php.core.index.PhpIndexingVisitorExtension;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.ArgumentValueType;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.compiler.DoctrineSourceElementRequestor;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.Entity;
import com.dubture.doctrine.core.model.IDoctrineModelElement;
import com.dubture.doctrine.core.utils.AnnotationUtils;


/**
 * Visits Doctrine Annotations.
 *
 * Currently indexes Entity classes and their corresponding
 * repositoryClass.
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class DoctrineIndexingVisitorExtension extends PhpIndexingVisitorExtension {

    private NamespaceDeclaration namespace;
    private IAnnotationModuleDeclaration decl;

    @Override
    public void setSourceModule(ISourceModule module) {
        super.setSourceModule(module);
        try {
			decl = AnnotationParserUtil.getModule(module);
		} catch (CoreException e) {
			Logger.logException(e);
		}

        this.sourceModule = module;
        indexPendingEntities();
        
    }
    
    protected void indexPendingEntities() 
    {
        List<Entity> pending = DoctrineBuilder.getPendingEntities();
        
        for (Entity entity : pending) {

            ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY,
                    0,
                    0,
                    entity.getElementName(),
                    null,
                    entity.getFullyQualifiedName());
            
            requestor.addReference(entityInfo);
        }
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration) throws Exception {
        if (typeDeclaration instanceof NamespaceDeclaration) {
            NamespaceDeclaration namespaceDeclaration = (NamespaceDeclaration) typeDeclaration;
            namespace = namespaceDeclaration;
        }

        return true;
    }

    protected void processClassDeclaration(ClassDeclaration classDeclaration, DeclarationInfo info) {
    	if (decl == null) {
    		return;
    	}
        List<Annotation> annotations = decl.readAnnotations((ASTNode)classDeclaration).getAnnotations();

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

        ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY,
                                                     classDeclaration.sourceStart(),
                                                     classDeclaration.sourceEnd(),
                                                     classDeclaration.getName(),
                                                     null,
                                                     qualifier);

        Logger.debugMSG("indexing entity: " + classDeclaration.getName() + " => " + qualifier);
        requestor.addReference(entityInfo);

        IArgumentValue repoArgumentValue = annotation.getArgumentValue("repositoryClass");
        if (repoArgumentValue == null || repoArgumentValue.getType() != ArgumentValueType.STRING) {
            return;
        }

        String repositoryClass = (String) repoArgumentValue.getValue();
        ReferenceInfo repositoryInfo = new ReferenceInfo(IDoctrineModelElement.REPOSITORY_CLASS,
                                                         classDeclaration.sourceStart(),
                                                         classDeclaration.sourceEnd(),
                                                         classDeclaration.getName(),
                                                         repositoryClass,
                                                         qualifier);

        Logger.debugMSG("indexing repository class: " + classDeclaration.getName() + " => " + repositoryClass);
        requestor.addReference(repositoryInfo);
    }
    
    @Override
    public void modifyDeclaration(ASTNode node, DeclarationInfo info) {
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
	}

}
