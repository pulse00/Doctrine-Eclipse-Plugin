package com.dubture.doctrine.internal.core.validation.validator;

import org.pdtextensions.semanticanalysis.validation.IValidatorContext;
import org.pdtextensions.semanticanalysis.validation.IValidatorParticipant;

import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;



/**
*
* {@link AnnotationValidatorVisitor} parses annotations from
* PHPDocBlocks.
*
* This will mainly be used for error reporting purposes
* and maybe syntax highlighting.
*
* For code-assistance in annotations, see 
*
* @see http://symfony.com/blog/symfony2-annotations-gets-better
* @author Robert Gruendler <r.gruendler@gmail.com>
*
*/
public class AnnotationValidator implements IValidatorParticipant{
	
	public static final String ID = "com.dubture.doctrine.core.annotationValidator";

	@Override
	public boolean allowDerived() {
		return false;
	}
		
	
	@Override
	public void validate(IValidatorContext context) throws Exception {
		if (context.getModuleDeclaration() != null) {
			IAnnotationModuleDeclaration annotationModule = AnnotationParserUtil.getModule(context.getSourceModule());
			if (annotationModule != null) {
				context.getModuleDeclaration().traverse(new AnnotationValidatorVisitor(context, (IAnnotationModuleDeclaration) annotationModule));
			}
		}
	}


}
