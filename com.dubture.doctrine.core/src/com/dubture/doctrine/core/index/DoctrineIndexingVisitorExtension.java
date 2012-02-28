package com.dubture.doctrine.core.index;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.IIndexingRequestor.ReferenceInfo;
import org.eclipse.php.core.index.PhpIndexingVisitorExtension;
import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.codeassist.strategies.PHPDocTagStrategy;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;

import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.IDoctrineModelElement;
import com.dubture.symfony.annotation.model.Annotation;
import com.dubture.symfony.annotation.model.ArgumentValueType;
import com.dubture.symfony.annotation.model.IArgumentValue;
import com.dubture.symfony.annotation.parser.AnnotationCommentParser;


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
public class DoctrineIndexingVisitorExtension extends
        PhpIndexingVisitorExtension {

    private NamespaceDeclaration namespace;

    @Override
    public void setSourceModule(ISourceModule module) {
        super.setSourceModule(module);
    }

    @Override
    public boolean visit(TypeDeclaration typeDeclaration) throws Exception {
        if (typeDeclaration instanceof ClassDeclaration) {
            ClassDeclaration classDeclaration = (ClassDeclaration) typeDeclaration;
            processClassDeclaration(classDeclaration);
        } else if (typeDeclaration instanceof NamespaceDeclaration) {
            NamespaceDeclaration namespaceDeclaration = (NamespaceDeclaration) typeDeclaration;
            namespace = namespaceDeclaration;
        }

        return true;
    }

    protected void processClassDeclaration(ClassDeclaration classDeclaration) {
        String[] includedClassNames = new String[]{"Entity"};
        List<Annotation> annotations = extractAnnotations(classDeclaration, includedClassNames);

        if (annotations.size() < 1) {
            return;
        }

        // Take only the last annotation
        Annotation annotation = annotations.get(annotations.size() - 1);

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

    // FIXME: This has been copied from AnnotationUtils that can be found in com.dubture.symfony.core
    protected List<Annotation> extractAnnotations(ClassDeclaration classDeclaration, String[] includedClassNames) {
        try {
            PHPDocBlock comment = classDeclaration.getPHPDoc();
            if (comment == null || comment.getCommentType() != Comment.TYPE_PHPDOC) {
                return new LinkedList<Annotation>();
            }

            int commentStartOffset = comment.sourceStart();
            String commentSource = comment.getLongDescription();
            if (commentSource.length() == 0) {
                commentSource = comment.getShortDescription();
            }

            AnnotationCommentParser parser = new AnnotationCommentParser(commentSource, commentStartOffset);
            parser.setExcludedClassNames(PHPDocTagStrategy.PHPDOC_TAGS);
            if (includedClassNames != null) {
                parser.setIncludedClassNames(includedClassNames);
            }

            return parser.parse();
        } catch (Exception exception) {
            Logger.logException("Unable to extract annotations from  class " + classDeclaration.getName(), exception);
            return new LinkedList<Annotation>();
        }
    }
}
