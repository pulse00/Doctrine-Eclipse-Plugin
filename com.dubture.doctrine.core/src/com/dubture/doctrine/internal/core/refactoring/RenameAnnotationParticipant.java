package com.dubture.doctrine.internal.core.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.manipulation.SourceModuleChange;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.SearchMatch;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.SearchRequestor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.eclipse.php.internal.core.compiler.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPFieldDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPMethodDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.TraitDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.UsePart;
import org.eclipse.php.internal.core.compiler.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;
import org.eclipse.php.internal.core.typeinference.PHPModelUtils;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import com.dubture.doctrine.annotation.model.AnnotationBlock;
import com.dubture.doctrine.annotation.model.AnnotationClass;
import com.dubture.doctrine.annotation.model.AnnotationVisitor;
import com.dubture.doctrine.core.AnnotationParserUtil;
import com.dubture.doctrine.core.DoctrineCorePlugin;
import com.dubture.doctrine.core.compiler.DoctrineFlags;
import com.dubture.doctrine.core.compiler.IAnnotationModuleDeclaration;
import com.dubture.doctrine.core.log.Logger;
import com.dubture.doctrine.core.preferences.DoctrineCoreConstants;

public class RenameAnnotationParticipant extends RenameParticipant {

	private IType fType;
	private String fFullName;

	@Override
	protected boolean initialize(Object element) {
		fType = (IType) element;
		fFullName = PHPModelUtils.getFullName(fType);
		try {
			if (DoctrineFlags.isAnnotation(((IType) element).getFlags())) {
				return true;
			}
		} catch (ModelException e) {
			Logger.logException(e);
		}
		return false;
	}

	@Override
	public String getName() {
		return "Rename doctrine annotation";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		final HashSet<ISourceModule> list = new HashSet<ISourceModule>();
		IScriptProject project = fType.getScriptProject();

		IDLTKSearchScope scope = SearchEngine.createSearchScope(project, IDLTKSearchScope.SOURCES | IDLTKSearchScope.APPLICATION_LIBRARIES);

		SearchPattern pattern = null;
		int matchMode = SearchPattern.R_EXACT_MATCH | SearchPattern.R_ERASURE_MATCH;

		SearchEngine engine = new SearchEngine();

		pattern = SearchPattern.createPattern(fType, IDLTKSearchConstants.ALL_OCCURRENCES, matchMode, PHPLanguageToolkit.getDefault());
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, new SearchRequestor() {
				@Override
				public void acceptSearchMatch(SearchMatch match) throws CoreException {
					IModelElement element = (IModelElement) match.getElement();
					if (element instanceof IMember) {
						list.add(((IMember) element).getSourceModule());
					} else if (element instanceof ISourceModule) {
						list.add((ISourceModule) element);
					}
				}
			}, new NullProgressMonitor());
		} catch (CoreException e) {
		}
		CompositeChange changes = new CompositeChange("Rename annotation class");
		boolean found = false;
		for (ISourceModule module : list) {
			ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(module);
			if (moduleDeclaration == null) {
				continue;
			}
			IAnnotationModuleDeclaration mod = AnnotationParserUtil.getModule(module);
			if (mod == null) {
				continue;
			}
			AnnotationChecker checker = new AnnotationChecker(mod);
			try {
				moduleDeclaration.traverse(checker);
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, DoctrineCorePlugin.ID, "Error during traverse", e)); //$NON-NLS-1$
			}
			if (checker.replaces.isEmpty()) {
				continue;
			}
			TextChange change;
			try {
				change = getTextChange(module.getResource());
				if (change == null) {
					change = new SourceModuleChange(module.getResource().getName(), module);
					change.setEdit(new MultiTextEdit());
					changes.add(change);
					found = true;
				}
				TextEditGroup textEditGroup = new TextEditGroup("Update annotation references");
				change.addTextEditGroup(textEditGroup);
				for (TextEdit edit : checker.replaces) {
					textEditGroup.addTextEdit(edit);
					change.addEdit(edit);
				}

			} catch (Exception e) {
				Logger.logException(e);
			}
		}

		return found ? changes : null;
	}

	@SuppressWarnings("restriction")
	private class AnnotationChecker extends PHPASTVisitor {
		private Map<String, String> parts;
		private Map<String, String> aliasedParts;
		private List<TextEdit> replaces = new LinkedList<TextEdit>();
		private IAnnotationModuleDeclaration annotationModule;

		public AnnotationChecker(IAnnotationModuleDeclaration annotationModule) {
			parts = new HashMap<String, String>();
			aliasedParts = new HashMap<String, String>();
			this.annotationModule = annotationModule;
		}

		@Override
		public boolean visit(NamespaceDeclaration s) throws Exception {
			parts = new HashMap<String, String>();
			aliasedParts = new HashMap<String, String>();
			return super.visit(s);
		}

		@Override
		public boolean visit(UseStatement s) {
			if (s.getStatementType() != UseStatement.T_NONE) {
				return false;
			}
			for (UsePart part : s.getParts()) {
				if (part.getNamespace() == null) {
					continue;
				}
				String fullName = part.getNamespace().getFullyQualifiedName().toLowerCase();
				if (part.getAlias() == null) {
					parts.put(part.getNamespace().getName().toLowerCase(), fullName);
				} else {
					aliasedParts.put(part.getAlias().getName().toLowerCase(), fullName);
				}

			}

			return false;
		}

		@Override
		public boolean visit(PHPMethodDeclaration s) throws Exception {
			check(s);

			return super.visit(s);
		}

		public boolean visit(PHPFieldDeclaration s) throws Exception {
			check(s);
			return super.visit(s);
		};

		@Override
		public boolean visit(TraitDeclaration s) throws Exception {
			check(s);
			return super.visit(s);
		}

		@Override
		public boolean visit(ClassDeclaration s) throws Exception {
			check(s);
			return super.visit(s);
		}

		private void check(ASTNode node) throws Exception {
			AnnotationBlock readAnnotations = annotationModule.readAnnotations(node);
			if (readAnnotations == null || readAnnotations.isEmpty()) {
				return;
			}

			readAnnotations.traverse(new AnnotationVisitor() {
				@Override
				public boolean visit(AnnotationClass node) {
					if (!node.hasNamespace() && aliasedParts.containsKey(node.getClassName().toLowerCase())) {
						return true;
					} else if (!fType.getElementName().equalsIgnoreCase(node.getClassName())) {
						return true;
					}
					String useName = (node.hasNamespace() ? node.getFirstNamespacePart() : node.getClassName()).toLowerCase();
					String realName;
					if (aliasedParts.containsKey(useName)) {
						realName = node.hasNamespace() ? aliasedParts.get(useName) + NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName()
								: aliasedParts.get(useName);
					} else if (parts.containsKey(useName)) {
						realName = node.hasNamespace() ? parts.get(useName) + NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName() : parts.get(useName);
					} else {
						realName = DoctrineCoreConstants.DEFAULT_ANNOTATION_NAMESPACE + NamespaceReference.NAMESPACE_SEPARATOR + node.getClassName();
					}
					if (!fFullName.equalsIgnoreCase(realName)) {
						return true;
					}

					int offset = node.getSourcePosition().startOffset + 1 + node.getNamespace().length();
					
					replaces.add(new ReplaceEdit(offset, node.getClassName().length(), getArguments().getNewName()));

					return super.visit(node);
				}
			});
		}
	}

}
