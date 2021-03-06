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
import org.eclipse.php.internal.core.util.text.PHPTextSequenceUtilities;
import org.eclipse.php.internal.core.util.text.TextSequence;

import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.internal.core.text.PHPDocTextSequenceUtilities;

/**
 * 
 * {@link AnnotationFieldContext} checks if we're in a valid PHPDocTag
 * completion context for annotations.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class AnnotationFieldContext extends AnnotationBodyContext {

	protected String prefix;

	@Override
	public boolean isValid(ISourceModule sourceModule, int offset, CompletionRequestor requestor) {

		if (!super.isValid(sourceModule, offset, requestor) == true) {
			return false;
		}

		try {
			TextSequence sequence = getStatementText();
			offset = sequence.length() - 1;
			char curr = sequence.charAt(offset);
			if (!PHPDocTextSequenceUtilities.isWhiteSpace(curr)) {
				if (curr == '(' || curr == ',') {
					prefix = "";
					return true;
				} else if (!PHPDocTextSequenceUtilities.isIdentPart(curr)) {
					return false;
				}
			}
			offset = PHPDocTextSequenceUtilities.readBackwardSpaces(sequence, offset);
			int start = PHPTextSequenceUtilities.readIdentifierStartIndex(sequence, offset, false);
			curr = sequence.charAt(PHPDocTextSequenceUtilities.readBackwardSpaces(sequence, start) - 1);
			if (curr == ',' || curr == '(') {
				prefix = sequence.toString().substring(start, offset).trim();
				return true;
			}
		} catch (Exception e) {
			Logger.logException(e);
		}

		return false;
	}

	public String getKeyPrefix() throws BadLocationException {
		return prefix;
	}

}
