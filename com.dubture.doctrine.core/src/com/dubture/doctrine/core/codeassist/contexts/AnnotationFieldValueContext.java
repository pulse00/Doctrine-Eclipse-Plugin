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
import org.eclipse.php.internal.core.util.text.PHPTextSequenceUtilities;
import org.eclipse.php.internal.core.util.text.TextSequence;

import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;
import com.dubture.doctrine.internal.core.text.PHPDocTextSequenceUtilities;

@SuppressWarnings("restriction")
public class AnnotationFieldValueContext extends AnnotationBodyContext {

	private String fieldName;
	private String valuePrefix;

	@Override
	public boolean isValid(ISourceModule sourceModule, int offset, CompletionRequestor requestor) {

		if (!super.isValid(sourceModule, offset, requestor) == true) {
			return false;
		}

		try {
			TextSequence sequence = getStatementText();
			offset = sequence.length() - 1;
			offset = PHPDocTextSequenceUtilities.readBackwardSpaces(sequence, offset);
			int start = PHPTextSequenceUtilities.readIdentifierStartIndex(sequence, offset, false);
			int end = PHPDocTextSequenceUtilities.readBackwardSpaces(sequence, start) - 1;
			StringBuilder prefixBuilder = new StringBuilder();
			for (; offset > 0; offset--) {
				char ch = sequence.charAt(offset);
				if (ch == '=') {
					valuePrefix = prefixBuilder.toString();
					break;
				} else if (ch == '(') {
					valuePrefix = prefixBuilder.toString();
					fieldName = DoctrineCoreConstants.DEFAULT_FIELD;
					return true;
				}
				prefixBuilder.insert(0, ch);
			}
			end = PHPDocTextSequenceUtilities.readBackwardSpaces(sequence, offset);
			start = PHPTextSequenceUtilities.readIdentifierStartIndex(sequence, offset, false);
			if (start >= end) {
				return false;
			}
			fieldName = sequence.toString().substring(start, end);

			return true;
		} catch (Exception e) {
			Logger.logException(e);
		}

		return false;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getValuePrefix() {
		return valuePrefix;
	}
}
