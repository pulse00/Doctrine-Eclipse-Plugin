/*******************************************************************************
 * This file is part of the doctrine eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist;

import org.eclipse.php.core.codeassist.ICompletionContext;
import org.eclipse.php.core.codeassist.ICompletionContextResolver;
import org.eclipse.php.internal.core.codeassist.contexts.CompletionContextResolver;

import com.dubture.doctrine.core.codeassist.contexts.AnnotationCompletionContext;
import com.dubture.doctrine.core.codeassist.contexts.AnnotationFieldContext;
import com.dubture.doctrine.core.codeassist.contexts.AnnotationFieldValueContext;
import com.dubture.doctrine.core.codeassist.contexts.AnnotationStringValueContext;


/**
 *
 * Context resolver for doctrine2.
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class DoctrineCompletionContextResolver extends CompletionContextResolver
	implements ICompletionContextResolver {


	@Override
	public ICompletionContext[] createContexts() {

		return new ICompletionContext[] {
				new AnnotationCompletionContext(),
				new AnnotationFieldContext(),
				new AnnotationFieldValueContext(),
				new AnnotationStringValueContext()
		};
	}
}
