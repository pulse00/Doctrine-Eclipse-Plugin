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
import org.eclipse.php.internal.core.util.text.PHPTextSequenceUtilities;
import org.eclipse.php.internal.core.util.text.TextSequence;
import org.eclipse.php.internal.core.util.text.TextSequenceUtilities;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.log.Logger;

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
public class AnnotationFieldContext extends PHPDocTagContext {

	private String prefix;
	private String annotationName;

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
			int start = sequence.toString().lastIndexOf("@");
			int end = sequence.toString().length();

			String line = sequence.toString().substring(start, end);

			// we're inside an annotation's parameters
			// can't complete this so far.

			if (!line.contains("(")) {
				return false;
			}
			TextSequence statementText = getStatementText();
			int readBackwardSpaces = PHPTextSequenceUtilities.readBackwardSpaces(statementText, statementText.length());

			int readIdentifierStartIndex = PHPTextSequenceUtilities.readIdentifierStartIndex(statementText,
					readBackwardSpaces, false);
			readBackwardSpaces = readIdentifierStartIndex - 1;
			while (Character.isWhitespace(statementText.charAt(readBackwardSpaces))
					|| statementText.charAt(readBackwardSpaces) == '*') {
				readBackwardSpaces -= 1;
			}
			if (statementText.charAt(readBackwardSpaces) != ',' && statementText.charAt(readBackwardSpaces) != '(') {
				return false;
			}
			prefix = statementText.subSequence(readIdentifierStartIndex, statementText.length()).toString().trim();
			annotationName = line.substring(1, line.indexOf('('));
			if (annotationName.length() == 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}

		return false;
	}

	public String getKeyPrefix() throws BadLocationException {
		return prefix;
	}

	public String getAnnotationName() {
		return annotationName;
	}
}
