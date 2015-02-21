package com.dubture.doctrine.core.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.codeassist.strategies.PHPDocTagStrategy;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.IPHPDocAwareDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPMethodDeclaration;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.parser.AnnotationCommentParser;
import com.dubture.doctrine.core.log.Logger;

public class AnnotationUtils {
	// FIXME: Those two have been copied from AnnotationUtils that can be found in com.dubture.symfony.core
    protected static final String[] PHPDOC_TAGS_EXTRA = {"api", "inheritdoc"};
    protected static final List<Annotation> EMPTY_ANNOTATIONS = new LinkedList<Annotation>();
	
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
			return extractAnnotations(parser, declaration, sourceModule.getSource());
		} catch (ModelException e) {
			Logger.logException("Unable to extract annotations from declaration " + getDeclarationName(declaration), e);
            return EMPTY_ANNOTATIONS;
		}
    }
    
    
    public static List<Annotation> extractAnnotations(AnnotationCommentParser parser,
                                                      IPHPDocAwareDeclaration declaration,
                                                      String source) {
    	try {
            PHPDocBlock comment = declaration.getPHPDoc();
            if (comment == null || comment.getCommentType() != Comment.TYPE_PHPDOC) {
                return EMPTY_ANNOTATIONS;
            }

            int commentStartOffset = comment.sourceStart();
            String commentSource = source.substring(commentStartOffset, comment.sourceEnd());

            return parser.parse(commentSource, commentStartOffset);
        } catch (Exception exception) {
            Logger.logException("Unable to extract annotations from declaration " + getDeclarationName(declaration), exception);
            return EMPTY_ANNOTATIONS;
        }
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

	public static AnnotationCommentParser createParser() {
		return createParser(PHPDOC_TAGS_EXTRA);
	}
}
