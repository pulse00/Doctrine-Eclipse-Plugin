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
import org.eclipse.php.internal.core.compiler.ast.nodes.IPHPDocAwareDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPMethodDeclaration;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.ArgumentValueType;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.Entity;
import com.dubture.doctrine.core.model.IDoctrineModelElement;


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

    // FIXME: Those two have been copied from AnnotationUtils that can be found in com.dubture.symfony.core
    protected static final String[] PHPDOC_TAGS_EXTRA = {"api", "inheritdoc"};
    protected static final List<Annotation> EMPTY_ANNOTATIONS = new LinkedList<Annotation>();

    private NamespaceDeclaration namespace;

    private ISourceModule sourceModule;
    private AnnotationCommentParser parser;

    @Override
    public void setSourceModule(ISourceModule module) {
        super.setSourceModule(module);

        this.sourceModule = module;
        this.parser = createParser(PHPDOC_TAGS_EXTRA);
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
        List<Annotation> annotations = extractAnnotations(parser, classDeclaration, sourceModule);

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


    /**
     * Parse a comment from a PHP doc aware declaration. By using the source module,
     * you ensure that source positions of the annotations will be set correctly
     * relative to the PHP file.
     *
     * @param parser The parser used to parse a comment
     * @param declaration The declaration to parse the comment from
     * @param sourceModule The source module to extract the comment from
     *
     * @return A list of valid annotations according to the parser
     */
    // FIXME: This has been copied from AnnotationUtils that can be found in com.dubture.symfony.core
    public static List<Annotation> extractAnnotations(AnnotationCommentParser parser,
                                                      IPHPDocAwareDeclaration declaration,
                                                      ISourceModule sourceModule) {
        try {
            PHPDocBlock comment = declaration.getPHPDoc();
            if (comment == null || comment.getCommentType() != Comment.TYPE_PHPDOC) {
                return EMPTY_ANNOTATIONS;
            }

            int commentStartOffset = comment.sourceStart();
            String source = sourceModule.getSource();
            String commentSource = source.substring(commentStartOffset, comment.sourceEnd());

            return parser.parse(commentSource, commentStartOffset);
        } catch (Exception exception) {
            Logger.logException("Unable to extract annotations from declaration " + getDeclarationName(declaration), exception);
            return EMPTY_ANNOTATIONS;
        }
    }

    /**
     * This is used for logging purpose. It takes an IPHPDocAwareDeclaration declaration
     * and tries to extract the name from it. For now, this method handle method and
     * class declaration.
     *
     * @param declaration The PHP doc aware declaration
     *
     * @return The name associated with this declaration if it can be found, string "unknown" otherwise
     */
    // FIXME: This has been copied from AnnotationUtils that can be found in com.dubture.symfony.core
    private static String getDeclarationName(IPHPDocAwareDeclaration declaration) {

        if (declaration instanceof PHPMethodDeclaration) {
            return "method: " + ((PHPMethodDeclaration) declaration).getName();
        }

        if (declaration instanceof ClassDeclaration) {
            return "class: " + ((ClassDeclaration) declaration).getName();
        }

        return "unknown";
    }

    /**
     * This will create a default {@link AnnotationCommentParser}. This default
     * parser will exclude base PHP doc tags and also some extra PHP doc tags (
     * {@literal @inheritdoc} and {@literal @api}).
     *
     * <p>
     * With this method, you can also specify which classes would like to include
     * in the list of annotations. This is useful when only want a certain types
     * of annotation like {@literal @Template} ones. The class names are the fully
     * qualified names. So, to include only {@literal "@Orm\Entity"} annotations,
     * you will need to pass the array new String[] {"@Orm\Entity"} to the method.
     * </p>
     *
     * @param includedClassNames The class names to includes, can be null
     *
     * @return A default {@link AnnotationCommentParser} set to include only the classes specified
     */
    // FIXME: This has been copied from AnnotationUtils that can be found in com.dubture.symfony.core
    public static AnnotationCommentParser createParser(String[] includedClassNames) {
        AnnotationCommentParser parser = new AnnotationCommentParser();
        parser.addExcludedClassNames(PHPDocTagStrategy.PHPDOC_TAGS);
        parser.addExcludedClassNames(PHPDOC_TAGS_EXTRA);
        if (includedClassNames != null) {
            parser.addIncludedClassNames(includedClassNames);
        }

        return parser;
    }
}
