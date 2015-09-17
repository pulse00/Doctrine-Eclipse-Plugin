/*******************************************************************************
 * This file is part of the doctrine eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist.contexts;

import org.eclipse.php.internal.core.util.text.TextSequence;

@SuppressWarnings("restriction")
public class AnnotationStringValueContext extends AnnotationFieldValueContext {

	private String valuePrefix;

	@Override
	public String getValuePrefix() {
		return valuePrefix;
	}

	@Override
	protected boolean detectEnvironment(TextSequence sequence, int offset) {
		StringBuilder possiblePrefix = new StringBuilder();
		for (; offset > 0; offset--) {
			char ch = sequence.charAt(offset);
			if (ch == '"') {
				valuePrefix = possiblePrefix.toString();

				return super.detectEnvironment(sequence, offset);
			}
			possiblePrefix.insert(0, ch);
		}

		return false;
	}
}
