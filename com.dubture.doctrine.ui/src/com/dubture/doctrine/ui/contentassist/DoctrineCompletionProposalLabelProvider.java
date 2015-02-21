/*******************************************************************************
 * This file is part of the Doctrine eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.contentassist;

import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalLabelProvider;

import com.dubture.doctrine.core.compiler.DoctrineFlags;
import com.dubture.doctrine.ui.log.Logger;


/**
 * A LabelProvider for Doctrine completion proposals.
 * 
 * 
 * @author "Robert Gruendler <r.gruendler@gmail.com>"
 *
 */
@SuppressWarnings("restriction")
public class DoctrineCompletionProposalLabelProvider extends
		PHPCompletionProposalLabelProvider {
	
	@Override
	public ImageDescriptor createTypeImageDescriptor(CompletionProposal proposal) {

		IModelElement element = proposal.getModelElement();
		
		
		try {
			if (element.getElementType() == IModelElement.TYPE && DoctrineFlags.isAnnotation(((IType) element).getFlags())) {
				return DLTKPluginImages.DESC_OBJS_ANNOTATION;
			}
		} catch (ModelException e) {
			Logger.logException(e);
		}
		
		return super.createTypeImageDescriptor(proposal);
	}
	
	
	
	@Override
	public String createTypeProposalLabel(CompletionProposal typeProposal) {
		return super.createTypeProposalLabel(typeProposal);
	}
	
}
