package com.dubture.doctrine.core.codeassist;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.codeassist.ScriptSelectionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.php.internal.core.PHPCorePlugin;
import org.eclipse.php.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.core.compiler.ast.nodes.PHPModuleDeclaration;
import org.eclipse.php.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.model.PHPModelAccess;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.php.internal.core.util.text.PHPTextSequenceUtilities;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;

import com.dubture.doctrine.core.DoctrineNature;
import com.dubture.doctrine.core.compiler.IDoctrineModifiers;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.internal.core.text.PHPDocTextSequenceUtilities;

@SuppressWarnings("restriction")
public class DoctrineSelectionEngine extends ScriptSelectionEngine {
	private final static IModelElement[] NONE = {};
	private final static String DEFAULT_ANNOTATION_QUALIFIER = "Doctrine\\Common\\Annotations\\Annotation"; //$NON-NLS-1$

	@Override
	public IModelElement[] select(IModuleSource sourceUnit, int offset, int end) {
		if (!PHPCorePlugin.toolkitInitialized) {
			return NONE;
		}

		if (end < offset) {
			end = offset + 1;
		}

		ISourceModule sourceModule = (ISourceModule) sourceUnit.getModelElement();

		try {
			if (sourceModule == null || !sourceModule.getScriptProject().getProject().hasNature(DoctrineNature.NATURE_ID)) {
				return NONE;
			}
		} catch (CoreException e) {
			Logger.logException(e);
			return NONE;
		}

		String content = sourceUnit.getSourceContents();
		if (content.length() <= offset) {
			return NONE;
		}
		IStructuredDocument document = null;
		IStructuredModel structuredModel = null;
		try {
			IFile file = (IFile) sourceUnit.getModelElement().getResource();
			if (file != null) {
				if (file.exists()) {
					structuredModel = StructuredModelManager.getModelManager().getExistingModelForRead(file);
					if (structuredModel != null) {
						document = structuredModel.getStructuredDocument();
					} else {
						document = StructuredModelManager.getModelManager().createStructuredDocumentFor(file);
					}
				} else {
					document = StructuredModelManager.getModelManager().createNewStructuredDocumentFor(file);
					document.set(sourceUnit.getSourceContents());
				}
			}
		} catch (Exception e) {
			Logger.logException(e);
		} finally {
			if (structuredModel != null) {
				structuredModel.releaseFromRead();
			}
		}
		if (document == null) {
			return NONE;
		}
		if (PHPDocTextSequenceUtilities.isPHPDoc(document, offset)) {
			return phpDocRead(sourceModule, document, offset, end);
		}

		return NONE;
	}

	private IModelElement[] phpDocRead(ISourceModule sourceModule, IStructuredDocument document, int offset, int end) {
		try {
			ModuleDeclaration moduleDeclaration = (PHPModuleDeclaration) SourceParserUtil.getModuleDeclaration(sourceModule);
			if (moduleDeclaration == null) {
				return NONE;
			}
			String annotationName = PHPDocTextSequenceUtilities.getAnnotationName(document, offset, end);
			if (annotationName != null) {
				String name = annotationName;
				String qualifier = null;
				IType namespace = PHPModelUtils.getCurrentNamespace(sourceModule, offset);
				if (annotationName.contains(String.valueOf(NamespaceReference.NAMESPACE_SEPARATOR))) {
					int i = name.lastIndexOf(NamespaceReference.NAMESPACE_SEPARATOR);
					qualifier = name.substring(0, i);
					name = name.substring(i + 1);

					String alias = qualifier;
					i = qualifier.indexOf(NamespaceReference.NAMESPACE_SEPARATOR);
					if (i != -1) {
						alias = qualifier.substring(0, i);
					}

					Map<String, UsePart> aliases = PHPModelUtils.getAliasToNSMap(alias, moduleDeclaration, offset, namespace, true);
					for (Entry<String, UsePart> entry : aliases.entrySet()) {
						if (alias.equalsIgnoreCase(entry.getKey())) {
							qualifier = entry.getValue().getNamespace().getFullyQualifiedName();
							break;
						}
					}
				} else {
					Map<String, UsePart> map = PHPModelUtils.getAliasToNSMap(annotationName, moduleDeclaration, offset, namespace, false);
					for (Entry<String, UsePart> entry : map.entrySet()) {
						if (annotationName.equalsIgnoreCase(entry.getKey())) {
							qualifier = entry.getValue().getNamespace().getNamespace().getName();
							name = entry.getValue().getNamespace().getName();
							break;
						}
					}
				}
				IDLTKSearchScope scope = createSearchScope(sourceModule);
				if (qualifier == null) {
					qualifier = DEFAULT_ANNOTATION_QUALIFIER; 
				}
				if (document.getChar(end + 1) == NamespaceReference.NAMESPACE_SEPARATOR) {
					StringBuilder sb = new StringBuilder(qualifier);
					sb.append(NamespaceReference.NAMESPACE_SEPARATOR).append(name);
					
					
					return PHPModelAccess.getDefault().findTypes(null, sb.toString(), MatchRule.EXACT, IDoctrineModifiers.AccNameSpace, 0, scope, null);
				}
				return PHPModelAccess.getDefault().findTypes(qualifier, name, MatchRule.EXACT, 0, 0, scope, null);
			} else {
				String text = document.getText();
				int nameStart = PHPTextSequenceUtilities.readNamespaceStartIndex(text, offset, false);
				int nameEnd = PHPTextSequenceUtilities.readNamespaceEndIndex(text, offset, false);
				if (nameStart > 0 && nameStart < nameEnd) {
					char next = text.charAt(PHPTextSequenceUtilities.readForwardSpaces(text, nameEnd));
					
					if (next != '=') {
						String name = text.substring(nameStart, nameEnd);
						if (nameStart + name.lastIndexOf('\\') > offset) {
							name = name.substring(0, name.lastIndexOf('\\'));
							return PHPModelAccess.getDefault().findNamespaces(null, name, MatchRule.EXACT, 0, 0, createSearchScope(sourceModule), null);
						} else {
							return PHPModelAccess.getDefault().findTypes(name, MatchRule.EXACT, 0, 0, createSearchScope(sourceModule), null);
						}
					}
				}
			}
		} catch (BadLocationException e) {
			Logger.logException(e);
		}
		return NONE;
	}

	private IDLTKSearchScope createSearchScope(ISourceModule sourceModule) {
		if (sourceModule.getScriptProject() != null) {
			return SearchEngine.createSearchScope(sourceModule.getScriptProject());
		}
		IProjectFragment projectFragment = (IProjectFragment) sourceModule.getAncestor(IModelElement.PROJECT_FRAGMENT);
		if (projectFragment != null) {
			return SearchEngine.createSearchScope(projectFragment);
		}
		return SearchEngine.createSearchScope(sourceModule);
	}
}
