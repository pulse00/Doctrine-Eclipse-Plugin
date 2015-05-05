package com.dubture.doctrine.test.text;

import org.eclipse.jface.text.IDocument;
import org.junit.Test;

/**
 * @author zulus
 */
public class TextSequenceUtilitiesTests {

	
	private static String testFile = "<?php\n" //$NON-NLS-1$
			+ "/**\n" //$NON-NLS-1$
			+ " * Description\n" //$NON-NLS-1$
			+ " * \n" //$NON-NLS-1$
			+ " * @author someone\n" //$NON-NLS-1$
			+ " * @MyAnnotation(\"value\", key={\n" //$NON-NLS-1$
			+ " *   @Abother\\Annotation\n" //$NON-NLS-1$
			+ " * })\n" //$NON-NLS-1$
			+ " * @AnnotationWithConst(MyClass::class\n" //$NON-NLS-1$
			+ " * /\n" //$NON-NLS-1$
			+ "class MyClass {}\n" //$NON-NLS-1$
			+ "?>"; //$NON-NLS-1$
	
	

	@Test
	public void extractPHPDoc() {
	}
}
