/*******************************************************************************
 * This file is part of the Doctrine eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.ui.contentassist;

import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;
import org.eclipse.php.core.PHPVersion;
import org.eclipse.php.internal.core.codeassist.AliasType;
import org.eclipse.php.internal.core.codeassist.ProposalExtraInfo;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.project.ProjectOptions;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.ui.PHPUiPlugin;
import org.eclipse.php.internal.ui.editor.contentassist.AutoActivationTrigger;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposal;
import org.eclipse.php.internal.ui.editor.contentassist.PHPCompletionProposalCollector;
import org.eclipse.swt.graphics.Image;

/**
 * @author "Robert Gruendler <r.gruendler@gmail.com>"
 *
 */
@SuppressWarnings("restriction")
public class DoctrineCompletionProposal extends PHPCompletionProposal {
	private static final String EMPTY_STRING = "";
	private static final String DOUBLE_COLON = "::";
	private IDocument document;
	private ISourceModule sourceModule;

	public DoctrineCompletionProposal(CompletionProposal typeProposal, IDocument document, ISourceModule cu,
			String replacementString, int replacementOffset, int replacementLength, Image image, String displayString,
			int relevance) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance);
		this.typeProposal = typeProposal;
		this.sourceModule = cu;
		this.document = document;
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		String replacementString = getReplacementString();
		char lastChar = replacementString.charAt(replacementString.length() - 1);
		if (lastChar == NamespaceReference.NAMESPACE_SEPARATOR || lastChar == '"' || lastChar == '=') {
			boolean enableAutoactivation = Platform.getPreferencesService().getBoolean(PHPCorePlugin.ID,
					PHPCoreConstants.CODEASSIST_AUTOACTIVATION, false, null);
			if (enableAutoactivation) {
				AutoActivationTrigger.register(document);
			}
		}
		super.apply(document, trigger, offset);
	}

	private boolean fReplacementStringComputed = false;
	private CompletionProposal typeProposal;

	public String getReplacementString() {
		if (!fReplacementStringComputed) {
			String replacementString = computeReplacementString();
			if (ProposalExtraInfo.isAddQuote(typeProposal.getExtraInfo())) {
				replacementString = "'" + replacementString + "'"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			setReplacementString(replacementString);
		}
		return super.getReplacementString();
	}

	private String computeReplacementString() {
		fReplacementStringComputed = true;
		IType type = (IType) typeProposal.getModelElement();

		if (ProposalExtraInfo.isClassInNamespace(typeProposal.getExtraInfo())) {
			return PHPModelUtils.getFullName(type);
			// String result = PHPModelUtils.getFullName(type);
			// if (ProposalExtraInfo.isAddQuote(typeProposal
			// .getExtraInfo())) {
			// result = "'" + result + "'";
			// }
			// return result;
		}

		String prefix = ""; //$NON-NLS-1$
		try {
			int flags = type.getFlags();
			IType currentNamespace = PHPModelUtils.getCurrentNamespaceIfAny(sourceModule, getReplacementOffset());
			IType namespace = PHPModelUtils.getCurrentNamespace(type);
			if (!PHPFlags.isNamespace(flags) && namespace == null && currentNamespace != null
					&& !ProjectOptions.getPhpVersion(sourceModule.getScriptProject().getProject())
							.isLessThan(PHPVersion.PHP5_3)
					&& document.getChar(getReplacementOffset() - 1) != NamespaceReference.NAMESPACE_SEPARATOR) {
				prefix = prefix + NamespaceReference.NAMESPACE_SEPARATOR;
			}
		} catch (ModelException e) {
			PHPUiPlugin.log(e);
		} catch (BadLocationException e) {
			PHPUiPlugin.log(e);
		}
		String suffix = getSuffix(type);
		String replacementString = null;
		if (typeProposal.getModelElement() instanceof AliasType) {
			replacementString = ((AliasType) typeProposal.getModelElement()).getAlias()
					+ typeProposal.getCompletion().substring(typeProposal.getName().length());
		} else {
			replacementString = super.getReplacementString();
		}
		return prefix + replacementString + suffix;
	}

	public String getSuffix(IType type) {
		String defaultResult = EMPTY_STRING;
		if (type instanceof AliasType) {
		}
		if (ProposalExtraInfo.isTypeOnly(typeProposal.getExtraInfo()) || !PHPModelUtils.hasStaticOrConstMember(type)) {
		}
		return defaultResult;
	}

	@Override
	public Object getExtraInfo() {
		return typeProposal.getExtraInfo();
	}
}
