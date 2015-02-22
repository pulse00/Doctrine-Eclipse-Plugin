/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 *
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.contentassist;

import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.text.completion.CompletionProposalLabelProvider;
import org.eclipse.dltk.ui.text.completion.IScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ProposalInfo;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.TypeProposalInfo;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.core.codeassist.ICompletionContextResolver;
import org.eclipse.php.core.codeassist.ICompletionStrategyFactory;
import org.eclipse.php.internal.core.codeassist.IPHPCompletionRequestorExtension;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalCollector;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.codeassist.DoctrineCompletionContextResolver;
import com.dubture.doctrine.core.codeassist.DoctrineCompletionStrategyFactory;

/**
 * The {@link DoctrineCompletionProposalCollector} is responsible for creating
 * custom proposals for Symfony elements like Routes and Services.
 *
 *
 * @author "Robert Gruendler <r.gruendler@gmail.com>"
 *
 */
@SuppressWarnings("restriction")
public class DoctrineCompletionProposalCollector extends PHPCompletionProposalCollector implements IPHPCompletionRequestorExtension {

	private CompletionProposalLabelProvider labelProvider;

	public DoctrineCompletionProposalCollector(IDocument document, ISourceModule cu, boolean explicit) {
		super(document, cu, explicit);

	}

	@Override
	public CompletionProposalLabelProvider getLabelProvider() {

		if (labelProvider == null)
			labelProvider = new DoctrineCompletionProposalLabelProvider();

		return labelProvider;

	}

	@Override
	protected IScriptCompletionProposal createTypeProposal(CompletionProposal proposal) {

		IModelElement element = proposal.getModelElement();

		if (element == null) {
			return null;
		}

		ProposalInfo proposalInfo = new TypeProposalInfo(getSourceModule()
				.getScriptProject(), proposal);
		ImageDescriptor imageDescriptor = ((PHPCompletionProposalLabelProvider) getLabelProvider())
				.createTypeImageDescriptor(proposal);
		

		// if (proposalInfo != null) {
		ScriptCompletionProposal doctrineProposal = generateDoctrineProposal(proposal, imageDescriptor);
		if (proposalInfo != null) {
			doctrineProposal.setProposalInfo(proposalInfo);
		}
		doctrineProposal.setRelevance(computeRelevance(proposal));
		return doctrineProposal;
	}


	private ScriptCompletionProposal generateDoctrineProposal(final CompletionProposal typeProposal, ImageDescriptor descriptor) {

		String completion = new String(typeProposal.getCompletion());
		int replaceStart = typeProposal.getReplaceStart();
		int length = getLength(typeProposal);
		Image image = getImage(descriptor);

		String displayString = ((DoctrineCompletionProposalLabelProvider) getLabelProvider()).createTypeProposalLabel(typeProposal);
		ScriptCompletionProposal scriptProposal = new DoctrineCompletionProposal(completion, replaceStart, length, image, displayString, 0) {
			@Override
			public Object getExtraInfo() {
				return typeProposal.getExtraInfo();
			}
		};

		return scriptProposal;

	}

	@Override
	protected String getNatureId() {
		return DoctrineNature.NATURE_ID;
	}

	@Override
	public ICompletionContextResolver[] getContextResolvers() {
		return new ICompletionContextResolver[] { new DoctrineCompletionContextResolver() };
	}

	@Override
	public ICompletionStrategyFactory[] getStrategyFactories() {
		return new ICompletionStrategyFactory[] { new DoctrineCompletionStrategyFactory() };
	}
}
