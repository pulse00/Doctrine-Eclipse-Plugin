/*******************************************************************************
 * This file is part of the doctrine eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.php.core.codeassist.ICompletionContext;
import org.eclipse.php.core.codeassist.ICompletionStrategy;
import org.eclipse.php.core.codeassist.ICompletionStrategyFactory;

import com.dubture.doctrine.core.codeassist.contexts.AnnotationCompletionContext;
import com.dubture.doctrine.core.codeassist.contexts.AnnotationFieldContext;
import com.dubture.doctrine.core.codeassist.strategies.AnnotationCompletionStrategy;
import com.dubture.doctrine.core.codeassist.strategies.AnnotationFieldStrategy;

/**
 * Factory class for CompletionStrategies.
 *
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
public class DoctrineCompletionStrategyFactory implements ICompletionStrategyFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public ICompletionStrategy[] create(ICompletionContext[] contexts) {

		List<ICompletionStrategy> result = new LinkedList<ICompletionStrategy>();

		for (ICompletionContext context : contexts) {
			Class contextClass = context.getClass();
			if (contextClass == AnnotationCompletionContext.class) {
				result.add(new AnnotationCompletionStrategy(context));
			} else if (contextClass == AnnotationFieldContext.class) {
				result.add(new AnnotationFieldStrategy(context));
			}
		}

		return (ICompletionStrategy[]) result.toArray(new ICompletionStrategy[result.size()]);

	}
}
 