/*******************************************************************************
 * This file is part of the Doctrine eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.contentassist;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;
import org.eclipse.dltk.ui.text.completion.ScriptContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalComputer;
import org.eclipse.php.internal.ui.editor.contentassist.PHPContentAssistInvocationContext;

@SuppressWarnings("restriction")
public class DoctrineCompletionProposalComputer extends PHPCompletionProposalComputer {

	@Override
	protected ScriptCompletionProposalCollector createCollector(ScriptContentAssistInvocationContext context) {
		boolean explicit = false;
		if (context instanceof PHPContentAssistInvocationContext) {
			explicit = ((PHPContentAssistInvocationContext) context).isExplicit();
		}
		return new DoctrineCompletionProposalCollector(context.getDocument(), context.getSourceModule(), explicit);

	}

	@Override
	protected List<ICompletionProposal> computeTemplateCompletionProposals(int offset,
			ScriptContentAssistInvocationContext context, IProgressMonitor monitor) {
		return Collections.emptyList();
	}
	
	@Override
	protected TemplateCompletionProcessor createTemplateProposalComputer(ScriptContentAssistInvocationContext context) {
		return null;
	}
}
