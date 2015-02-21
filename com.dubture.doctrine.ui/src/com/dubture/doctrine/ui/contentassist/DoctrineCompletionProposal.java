/*******************************************************************************
 * This file is part of the Doctrine eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.contentassist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.ui.editor.contentassist.AutoActivationTrigger;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposal;
import org.eclipse.swt.graphics.Image;

/**
 * @author "Robert Gruendler <r.gruendler@gmail.com>"
 *
 */
@SuppressWarnings("restriction")
public class DoctrineCompletionProposal extends PHPCompletionProposal {

	public DoctrineCompletionProposal(String replacementString,
			int replacementOffset, int replacementLength, Image image,
			String displayString, int relevance) {
		super(replacementString, replacementOffset, replacementLength, image,
				displayString, relevance);

	}
	
	@Override
	public void apply(IDocument document, char trigger, int offset) {
		String replacementString = getReplacementString();
		char lastChar = replacementString.charAt(replacementString
				.length() - 1);
		super.apply(document, trigger, offset);
		if (lastChar == NamespaceReference.NAMESPACE_SEPARATOR) {
			AutoActivationTrigger.register(document);
		}
	}
		

	
	
	@Override
	public String getReplacementString() {
		return super.getReplacementString();
	}
	
}
