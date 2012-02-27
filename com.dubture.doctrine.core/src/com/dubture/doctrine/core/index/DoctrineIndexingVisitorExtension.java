package com.dubture.doctrine.core.index;

import java.io.BufferedReader;
import java.io.StringReader;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.index2.IIndexingRequestor.ReferenceInfo;
import org.eclipse.php.core.index.PhpIndexingVisitorExtension;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;

import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.model.IDoctrineModelElement;
import com.dubture.symfony.annotation.model.ArgumentValueType;
import com.dubture.symfony.annotation.model.IArgumentValue;
import com.dubture.symfony.annotation.parser.antlr.AnnotationLexer;
import com.dubture.symfony.annotation.parser.antlr.AnnotationParser;
import com.dubture.symfony.annotation.parser.antlr.error.IAnnotationErrorReporter;
import com.dubture.symfony.annotation.parser.tree.AnnotationCommonTree;
import com.dubture.symfony.annotation.parser.tree.AnnotationCommonTreeAdaptor;
import com.dubture.symfony.annotation.parser.tree.visitor.AnnotationNodeVisitor;


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
    public boolean visit(TypeDeclaration s) throws Exception {

        if (s instanceof ClassDeclaration) {

            ClassDeclaration clazz = (ClassDeclaration) s;
            parseClassDocBlock(clazz);

        } else if (s instanceof NamespaceDeclaration) {

            NamespaceDeclaration ns = (NamespaceDeclaration) s;
            namespace = ns;
        }

        return true;
    }


    protected void parseClassDocBlock(ClassDeclaration clazz) {

        PHPDocBlock phpDoc = clazz.getPHPDoc();

        if (phpDoc == null)
            return;

        String comment = "";

        comment = phpDoc.getLongDescription();

        if (comment == null || comment.length() == 0)
            comment = phpDoc.getShortDescription();

        BufferedReader buffer = new BufferedReader(new StringReader(comment));

        try {

            String line;

            while((line = buffer.readLine()) != null) {

                int start = line.indexOf('@');
                int end = line.length()-1;

                if (start == -1) {
                    continue;
                }
                String annotation = line.substring(start, end+1);
                CharStream content = new ANTLRStringStream(annotation);

                IAnnotationErrorReporter reporter = new IAnnotationErrorReporter() {

                    @Override
                    public void reportError(String header, String message,
                            RecognitionException e) {

                    }
                };

                AnnotationLexer lexer = new AnnotationLexer(content, reporter);

                AnnotationParser parser = new AnnotationParser(new CommonTokenStream(lexer));
                parser.setErrorReporter(reporter);

                parser.setTreeAdaptor(new AnnotationCommonTreeAdaptor());
                AnnotationParser.annotation_return root;

                root = parser.annotation();
                AnnotationCommonTree tree = (AnnotationCommonTree) root.getTree();
                AnnotationNodeVisitor visitor = new AnnotationNodeVisitor();

                if (tree == null) {
                    return;
                }

                tree.accept(visitor);

                if ("Entity".equals(visitor.getAnnotation().getClassName())) {

                    String qualifier = null;
                    if (namespace != null) {
                        qualifier = namespace.getName();
                    }

                    ReferenceInfo entityInfo = new ReferenceInfo(IDoctrineModelElement.ENTITY, clazz.sourceStart(), clazz.sourceEnd(), clazz.getName(), null, qualifier);

                    Logger.debugMSG("indexing entity: " + clazz.getName() + " => " + qualifier);
                    requestor.addReference(entityInfo);

                    IArgumentValue repoArgumentValue = visitor.getAnnotation().getArgumentValue("repositoryClass");

                    if (repoArgumentValue == null || repoArgumentValue.getType() != ArgumentValueType.STRING)
                        return;

                    String repo = (String) repoArgumentValue.getValue();
                    ReferenceInfo info = new ReferenceInfo(IDoctrineModelElement.REPOSITORY_CLASS, clazz.sourceStart(), clazz.sourceEnd(), clazz.getName(), repo, qualifier);

                    Logger.debugMSG("indexing repository class: " + clazz.getName() + " => " + repo);
                    requestor.addReference(info);

                }
            }
        } catch (Exception e) {
            Logger.logException(e);
        }
    }
}
