/*******************************************************************************
 * This file is part of the doctrine eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist.contexts;

import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.php.internal.core.codeassist.contexts.PHPDocTagContext;
import org.eclipse.php.internal.core.util.text.TextSequence;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.internal.core.text.PHPDocTextSequenceUtilities;

@SuppressWarnings("restriction")
abstract public class AnnotationBodyContext extends PHPDocTagContext {

	private String annotationName;
	
	/**
	 * Read environment, set prefix, annotation, fieldName
	 * 
	 * @param inString
	 * @return
	 */
	protected boolean detectEnvironment(TextSequence sequence, int offset) {
		offset = PHPDocTextSequenceUtilities.findAnnotationBodyStart(sequence, offset);
		if (offset == -1) {
			return false;
		}
		offset = PHPDocTextSequenceUtilities.readBackwardSpaces(sequence, offset);
		StringBuilder name = new StringBuilder();
		for (; offset >= 0; offset --) {
			char ch = sequence.charAt(offset);
			if (PHPDocTextSequenceUtilities.isIdentPart(ch)) {
				name.insert(0, ch);
			} else if (ch == '@') {
				if (name.length() == 0) {
					return false;
				}
				annotationName = name.toString();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isValid(ISourceModule sourceModule, int offset, CompletionRequestor requestor) {

		if (!super.isValid(sourceModule, offset, requestor) == true) {
			return false;
		}

		try {
			// wrong nature
			if (!sourceModule.getScriptProject().getProject().hasNature(DoctrineNature.NATURE_ID)) {
				return false;
			}
			TextSequence sequence = getStatementText();
			return detectEnvironment(sequence, sequence.length() - 1);
		} catch (Exception e) {
			Logger.logException(e);
		}
		return false;
	}

	public String getAnnotationName() {
		return annotationName;
	}
}
