/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist.strategies;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.SourceRange;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.internal.core.ModelElement;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.core.hierarchy.FakeType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.php.core.codeassist.ICompletionContext;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.codeassist.AliasType;
import org.eclipse.php.internal.core.codeassist.CodeAssistUtils;
import org.eclipse.php.internal.core.codeassist.ICompletionReporter;
import org.eclipse.php.internal.core.codeassist.ProposalExtraInfo;
import org.eclipse.php.internal.core.codeassist.strategies.PHPDocTagStrategy;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;

import com.dubture.doctrine.core.codeassist.contexts.AnnotationCompletionContext;
import com.dubture.doctrine.core.compiler.DoctrineFlags;
import com.dubture.doctrine.core.compiler.IDoctrineModifiers;

/**
 * 
 * The {@link AnnotationCompletionStrategy} parses the UseStatements of the
 * current class and reports the aliases to the completion engine:
 * 
 * <pre>
 * 
 *   use Doctrine\ORM\Mapping as ORM;
 *   
 *   ...
 *   
 *   /**
 *   @ | <-- add ORM\ to the code completion suggestions
 * 
 * </pre>
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 */
@SuppressWarnings({ "restriction" })
public class AnnotationCompletionStrategy extends PHPDocTagStrategy {

	private int trueFlag = IDoctrineModifiers.AccAnnotation;
	private int falseFlag = 0;

	public AnnotationCompletionStrategy(ICompletionContext context) {
		super(context);
	}

	@Override
	public void apply(final ICompletionReporter reporter) throws BadLocationException {

		ICompletionContext ctx = getContext();

		if (!(ctx instanceof AnnotationCompletionContext)) {
			return;
		}

		AnnotationCompletionContext context = (AnnotationCompletionContext) ctx;
		int target = context.getTarget();
		if (target == -1) {
			return;
		}
		trueFlag = target;
		ISourceRange replaceRange = getReplacementRange(context);
		IDLTKSearchScope scope = createSearchScope();
		String prefix = context.getPrefix();
		String name = prefix;
		String qualifier = null;
		ISourceModule sourceModule = context.getSourceModule();
		if (sourceModule == null) {
			return;
		}
		
		ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(sourceModule);
		IType namespace = PHPModelUtils.getCurrentNamespace(sourceModule, context.getOffset());
		Set<IType> collected = new HashSet<IType>();
		if (prefix.contains(String.valueOf(NamespaceReference.NAMESPACE_SEPARATOR))) {
			int i = name.lastIndexOf(NamespaceReference.NAMESPACE_SEPARATOR);
			qualifier = name.substring(0, i);
			name = name.substring(i + 1);

			String alias = qualifier;
			i = qualifier.indexOf(NamespaceReference.NAMESPACE_SEPARATOR);
			if (i != -1) {
				alias = qualifier.substring(0, i);
			}
			int length = name.length();

			int start = context.getOffset() - length;
			int prefixEnd = context.getPrefixEnd();

			if (start + length < prefixEnd) {
				length = prefixEnd - start;
			}
			replaceRange = new SourceRange(start, length); //set valid replace range
			Map<String, UsePart> aliases = PHPModelUtils.getAliasToNSMap(alias, moduleDeclaration, context.getOffset(), namespace, false);
			for (Entry<String, UsePart> entry : aliases.entrySet()) {
				if (alias.equalsIgnoreCase(entry.getKey())) {
					qualifier = entry.getValue().getNamespace().getFullyQualifiedName();
					break;
				}
			}
		} else {
			Map<String, UsePart> map = PHPModelUtils.getAliasToNSMap("", moduleDeclaration, context.getOffset(), namespace, false); //$NON-NLS-1$
			for (Entry<String, UsePart> entry : map.entrySet()) {
				if (!CodeAssistUtils.startsWithIgnoreCase(entry.getKey(), name)) {
					continue;
				}
				IType[] findTypes = PhpModelAccess.getDefault().findTypes(entry.getValue().getNamespace().getFullyQualifiedName(), MatchRule.EXACT, 0, 0, scope, null);
				if (findTypes.length != 0) {
					try {
						if (DoctrineFlags.isAnnotation(findTypes[0].getFlags()) && (findTypes[0].getFlags() & target) == 0) {
							continue;
						}
						collected.add(findTypes[0]);
						reporter.reportType(new AliasType((SourceType)findTypes[0], entry.getValue().getNamespace().getName(), entry.getKey()), DoctrineFlags.isNamespace(findTypes[0].getFlags()) ? "\\" : "()", replaceRange, Integer.valueOf(0), 10);
					} catch (ModelException e) {
						Logger.logException(e);
					}
				} else {
					reporter.reportType(new FakeType((ModelElement)sourceModule, entry.getKey(), IDoctrineModifiers.AccNameSpace), "\\", replaceRange, Integer.valueOf(ProposalExtraInfo.TYPE_ONLY) | ProposalExtraInfo.NO_INSERT_USE, 10);
				}
			}
		}
		

		IType[] types = getTypes(context, scope, qualifier, name.trim());
		String suffix = "()";
		for (IType type : types) {
			if (!collected.contains(type)) {
				reporter.reportType(type, suffix, replaceRange);
			}
		}
	}

	private IType[] getTypes(AnnotationCompletionContext context, IDLTKSearchScope scope, String qualifier, String prefix) {
		if (context.getCompletionRequestor().isContextInformationMode()) {
			return PhpModelAccess.getDefault().findTypes(qualifier, prefix, MatchRule.EXACT, trueFlag, falseFlag, scope, null);
		}

		List<IType> result = new LinkedList<IType>();
		if (prefix.length() > 1 && prefix.toUpperCase().equals(prefix)) {
			// Search by camel-case
			IType[] types = PhpModelAccess.getDefault().findTypes(qualifier, prefix, MatchRule.CAMEL_CASE, trueFlag, falseFlag, scope, null);
			result.addAll(Arrays.asList(types));
		}

		IType[] types = PhpModelAccess.getDefault().findTypes(qualifier, prefix, MatchRule.PREFIX, trueFlag, falseFlag, scope, null);
		result.addAll(Arrays.asList(types));

		return (IType[]) result.toArray(new IType[result.size()]);
	}

}
