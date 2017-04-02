/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist.strategies;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.php.core.codeassist.ICompletionContext;
import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.core.codeassist.ICompletionReporter;
import org.eclipse.php.internal.core.codeassist.strategies.PHPDocTagStrategy;
import org.eclipse.php.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;

import com.dubture.doctrine.core.codeassist.contexts.AnnotationFieldContext;
import com.dubture.doctrine.core.compiler.IDoctrineModifiers;

/**
 * 
 * The {@link AnnotationFieldStrategy} parses the UseStatements of the current
 * class and reports the aliases to the completion engine:
 * 
 * <pre>
 * 
 *   use Doctrine\ORM\Mapping as ORM;
 *   
 *   ...
 *   
 *   /**
 *   &#64; | <-- add ORM\ to the code completion suggestions
 * 
 * </pre>
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 */
@SuppressWarnings({ "restriction" })
public class AnnotationFieldStrategy extends PHPDocTagStrategy {

	private int trueFlag = IDoctrineModifiers.AccAnnotation;
	private int falseFlag = 0;

	public AnnotationFieldStrategy(ICompletionContext context) {
		super(context);
	}

	@Override
	public void apply(final ICompletionReporter reporter) throws BadLocationException {

		ICompletionContext ctx = getContext();

		if (!(ctx instanceof AnnotationFieldContext)) {
			return;
		}

		AnnotationFieldContext context = (AnnotationFieldContext) ctx;
		ISourceRange replaceRange = getReplacementRange(context);
		IDLTKSearchScope scope = createSearchScope();
		ISourceModule sourceModule = context.getSourceModule();
		if (sourceModule == null) {
			return;
		}
		ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(sourceModule);
		IType namespace = PHPModelUtils.getCurrentNamespace(sourceModule, context.getOffset());
		String prefix = context.getAnnotationName();
		String name = prefix;
		String qualifier = null;

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
			Map<String, UsePart> aliases = PHPModelUtils.getAliasToNSMap(alias, moduleDeclaration, context.getOffset(),
					namespace, false);
			for (Entry<String, UsePart> entry : aliases.entrySet()) {
				if (alias.equalsIgnoreCase(entry.getKey())) {
					qualifier = entry.getValue().getNamespace().getFullyQualifiedName();
					break;
				}
			}
		} else {
			Map<String, UsePart> map = PHPModelUtils.getAliasToNSMap("", moduleDeclaration, context.getOffset(), //$NON-NLS-1$
					namespace, false);
			for (Entry<String, UsePart> entry : map.entrySet()) {
				if (!StringUtils.startsWithIgnoreCase(entry.getKey(), name)) {
					continue;
				}
				name = entry.getValue().getNamespace().getName();
				qualifier = entry.getValue().getNamespace().getNamespace().getName();
			}
		}
		IType[] findTypes = PhpModelAccess.getDefault().findTypes(qualifier, name, MatchRule.EXACT, trueFlag, falseFlag,
				scope, null);
		for (IType type : findTypes) {
			try {
				for (IField f : PHPModelUtils.getTypeHierarchyField(type,
						getCompanion().getSuperTypeHierarchy(type, null), "$" + context.getKeyPrefix(), false,
						new NullProgressMonitor())) {
					if (PHPFlags.isPublic(f.getFlags()) && !PHPFlags.isStatic(f.getFlags())
							&& !"$value".equals(f.getElementName())) {
						String suffix = "=";
						if ("string".equalsIgnoreCase(f.getType())) {
							suffix = "=\"\"";
						}
						reporter.reportField(f, suffix, replaceRange, true, 0, ICompletionReporter.RELEVANCE_KEYWORD + 1);
						continue;
						// TODO FOrmatter settings:
					}
					IMethod[] setter = PHPModelUtils.getTypeHierarchyMethod(type,
							getCompanion().getSuperTypeHierarchy(type, null), "set" + f.getElementName().substring(1),
							true, new NullProgressMonitor());
					if (setter != null && setter.length > 0 && PHPFlags.isPublic(setter[0].getFlags())
							&& !PHPFlags.isStatic(setter[0].getFlags())) {
						reporter.reportField(f, "=", replaceRange, true, 0, ICompletionReporter.RELEVANCE_KEYWORD + 1);
						continue;
					}
				}
			} catch (ModelException e) {
				Logger.logException(e);
			} catch (CoreException e) {
				Logger.logException(e);
			}
		}

	}

}
