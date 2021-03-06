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
import org.eclipse.php.internal.core.documentModel.parser.regions.IPHPScriptRegion;
import org.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;
import org.eclipse.php.internal.core.documentModel.partitioner.PHPPartitionTypes;
import org.eclipse.php.internal.core.util.text.TextSequence;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.IDoctrineModifiers;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.internal.core.text.PHPDocTextSequenceUtilities;

/**
 * 
 * {@link AnnotationCompletionContext} checks if we're in a valid PHPDocTag
 * completion context for annotations.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class AnnotationCompletionContext extends PHPDocTagContext {

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
			if (line.contains("(") || line.contains("[") || line.contains("{") || line.contains(" ")
					|| line.contains("\t")) {
				return false;
			}
			line = line.trim();
			if (line.trim().endsWith("*")) { //$NON-NLS-1$
				return false;
			}
			for (int i = line.length() - 1; i > 0; i--) {
				char ch = line.charAt(i);
				if (!PHPDocTextSequenceUtilities.isIdentPart(ch)) {
					return false;
				}
			}			
			return true;
		} catch (Exception e) {
			Logger.logException(e);
			return false;
		}
	}

	public int getTarget() {
		final int offset = getCompanion().getOffset();
		IStructuredDocumentRegion sdRegion = getCompanion().getDocument().getRegionAtCharacterOffset(offset);
		ITextRegion textRegion = sdRegion.getRegionAtCharacterOffset(offset);
		if (!(textRegion instanceof IPHPScriptRegion)) {
			return -1;
		}

		IPHPScriptRegion phpScriptRegion = (IPHPScriptRegion) textRegion;
		int position = offset;
		try {
			textRegion = phpScriptRegion.getPHPToken(position - phpScriptRegion.getStart() - sdRegion.getStartOffset());
			if (textRegion != null && PHPDocTextSequenceUtilities.isInsideAnnotation(
					sdRegion.getParentDocument().get(textRegion.getStart(),
							textRegion.getEnd() + sdRegion.getStartOffset() > offset
									? textRegion.getEnd() + sdRegion.getStartOffset() : offset),
					offset - textRegion.getStart())) {
				return IDoctrineModifiers.AccTargetAnnotation;
			}
			while (textRegion != null) {

				if (PHPPartitionTypes.isPHPCommentState(textRegion.getType())
						|| PHPRegionTypes.WHITESPACE.equals(textRegion.getType())) {
					textRegion = phpScriptRegion.getPHPToken(textRegion.getEnd() + 1);
					continue;
				}
				if (PHPRegionTypes.PHP_CURLY_OPEN.equals(textRegion.getType())
						|| PHPRegionTypes.PHP_SEMICOLON.equals(textRegion.getType())) {
					return -1;
				}
				if (PHPRegionTypes.PHP_FUNCTION.equals(textRegion.getType())) {
					return IDoctrineModifiers.AccTargetMethod;
				}
				if (PHPRegionTypes.PHP_VAR.equals(textRegion.getType())) {
					return IDoctrineModifiers.AccTargetField;
				}
				if (PHPRegionTypes.PHP_VARIABLE.equals(textRegion.getType())) {
					return IDoctrineModifiers.AccTargetField;
				}
				if (PHPRegionTypes.PHP_CLASS.equals(textRegion.getType())) {
					return IDoctrineModifiers.AccTargetClass;
				}
				textRegion = phpScriptRegion.getPHPToken(textRegion.getEnd() + 1);
			}
		} catch (BadLocationException e) {
			Logger.logException(e);
		}

		return -1;
	}
}
