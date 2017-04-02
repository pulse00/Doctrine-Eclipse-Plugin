/*******************************************************************************
 * This file is part of the Symfony eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.codeassist.strategies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.php.core.codeassist.ICompletionContext;
import org.eclipse.php.internal.core.Logger;
import org.eclipse.php.internal.core.codeassist.CompletionFlag;
import org.eclipse.php.core.codeassist.ICompletionReporter;
import org.eclipse.php.internal.core.codeassist.IPHPCompletionRequestor;
import org.eclipse.php.internal.core.codeassist.contexts.AbstractCompletionContext;
import org.eclipse.php.internal.core.codeassist.strategies.PHPDocTagStrategy;
import org.eclipse.php.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;

import com.dubture.doctrine.annotation.model.Annotation;
import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.ArrayValue;
import com.dubture.doctrine.annotation.model.IArgumentValue;
import com.dubture.doctrine.annotation.model.StringValue;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.codeassist.contexts.AnnotationFieldValueContext;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.compiler.IDoctrineModifiers;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;

/**
 * this class return enum values for field
 */
@SuppressWarnings({ "restriction" })
public class AnnotationEnumStrategy extends PHPDocTagStrategy {

	private int trueFlag = IDoctrineModifiers.AccAnnotation;
	private int falseFlag = 0;
	private static final Map<String, String[]> builtIn = new HashMap<String, String[]>();

	static {
		builtIn.put(DoctrineCoreConstants.COLUMN_ANNOTATION + "#$type",
				new String[] { "smallint", "integer", "bigint", "decimal", "string", "text", "guid", "binary", "blob",
						"boolean", "date", "datetime", "datetimez", "time", "dateinterval", "array", "simple_array",
						"json_array", "object" });
		builtIn.put(DoctrineCoreConstants.TARGET_ANNOTATION + "#$value",
				new String[] { "ALL", "CLASS", "METHOD", "PROPERTY", "ANNOTATION" });
	}

	public AnnotationEnumStrategy(ICompletionContext context) {
		super(context);
	}

	@Override
	public void apply(final ICompletionReporter reporter) throws BadLocationException {

		ICompletionContext ctx = getContext();

		if (!(ctx instanceof AnnotationFieldValueContext)) {
			return;
		}

		AnnotationFieldValueContext context = (AnnotationFieldValueContext) ctx;
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
				if (!name.equalsIgnoreCase(entry.getKey())) {
					continue;
				}
				name = entry.getValue().getNamespace().getName();
				qualifier = entry.getValue().getNamespace().getNamespace().getName();
			}
			if (qualifier == null) {
				qualifier = DoctrineCoreConstants.DEFAULT_ANNOTATION_NAMESPACE;
			}
		}
		IType[] findTypes = PhpModelAccess.getDefault().findTypes(qualifier, name, MatchRule.EXACT, trueFlag, falseFlag,
				scope, null);
		for (IType type : findTypes) {
			try {
				for (IField f : PHPModelUtils.getTypeHierarchyField(type,
						getCompanion().getSuperTypeHierarchy(type, null), "$" + context.getFieldName(), false,
						new NullProgressMonitor())) {
					collectValues(type, f, reporter, replaceRange, context);
				}
			} catch (ModelException e) {
				Logger.logException(e);
			} catch (CoreException e) {
				Logger.logException(e);
			}
		}

	}

	private void collectValues(IType type, IField field, ICompletionReporter reporter, ISourceRange replaceRange,
			AnnotationFieldValueContext context) throws CoreException {
		String n = PHPModelUtils.getFullName(type) + "#" + field.getElementName();
		IPHPCompletionRequestor phpCompletionRequestor = (IPHPCompletionRequestor) ((AbstractCompletionContext)getContext()).getCompletionRequestor();
		if (builtIn.containsKey(n)) {
			phpCompletionRequestor.addFlag(CompletionFlag.STOP_REPORT_TYPE);
			for (String key : builtIn.get(n)) {
				if (StringUtils.startsWithIgnoreCase(key, context.getValuePrefix())) {
					reporter.reportKeyword(key, "", replaceRange);
				}
			}
			
			return;
		}
		
		IAnnotationModuleDeclaration module = AnnotationParserUtil.getModule(type.getAncestor(ISourceModule.class));
		if (module == null) {
			return;
		}

		AnnotationBlock annotations = module.readAnnotations(field);
		// XXX Resolve real name
		for (Annotation ann : annotations.findAnnotations("Enum")) {
			phpCompletionRequestor.addFlag(CompletionFlag.STOP_REPORT_TYPE);
			IArgumentValue val = ann.getArgumentValue(DoctrineCoreConstants.DEFAULT_FIELD);
			if (val == null && ann.getArguments().size() > 0) {
				val = ann.getArgumentValue(0);
			}
			if (val instanceof ArrayValue) {
				List<IArgumentValue> arr = (List<IArgumentValue>) val.getValue();
				for (IArgumentValue en : arr) {
					if (en instanceof StringValue) {
						String string = (String) en.getValue();
						if (StringUtils.startsWithIgnoreCase(string, context.getValuePrefix())) {
							reporter.reportKeyword(string, "", replaceRange);
						}
					}
				}
			}
		}
	}

}
