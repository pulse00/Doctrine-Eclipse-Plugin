package com.dubture.doctrine.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.internal.core.SourceField;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.ui.dialogs.OpenTypeSelectionDialog2;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;
import org.eclipse.php.internal.core.ast.nodes.AST;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.ui.PHPUILanguageToolkit;
import org.eclipse.php.internal.ui.actions.SelectionHandler;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import com.dubture.doctrine.ui.dialog.GetterSetterDialog;

/**
 * 
 * Generates getters for private fields.
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class GenerateGettersHandler extends SelectionHandler implements
		IHandler {
	
	private IEditorPart editorPart;
	private PHPStructuredEditor textEditor;
	private IDocument document;
	private SourceField field;
	private Map options;
	private SourceType type;


	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IModelElement element = getCurrentModelElement(event);
		
		if (!(element instanceof SourceField)) {
			return null;
		}
		
		try {
			
			initialize(event, element);
			
			List<SourceField> fields = getFields();			
			List<IMethod> methods = getMethods();
			
			final Shell p = DLTKUIPlugin.getActiveWorkbenchShell();
			GetterSetterDialog dialog = new GetterSetterDialog(p, type);
			
//			OpenTypeSelectionDialog2 dialog = getDialog(IDLTKSearchConstants.TYPE, "Superclass selection", "Select superclass");
//
			int result = dialog.open();
			
			ISourceModule source = field.getSourceModule();			
			String name = field.getElementName().replace("$", "");			
			StringBuffer buffer = new StringBuffer(name);			
			buffer.replace(0, 1, Character.toString(Character.toUpperCase(name.charAt(0))));			
			name = buffer.toString();			
			String identifier = "get" + name;			

			
			ASTParser parser = ASTParser
					.newParser(source);
			parser.setSource(document.get().toCharArray());
			
			Program program = parser.createAST(null);			
			program.recordModifications();
			AST ast = program.getAST();			
			List<FormalParameter> params = new ArrayList<FormalParameter>();
			Block block = ast.newBlock();

			FunctionDeclaration function = ast.newFunctionDeclaration(ast.newIdentifier(identifier), params, block, false);
			MethodDeclaration method = ast.newMethodDeclaration(Modifiers.AccPublic, function);
			ISourceRange range = type.getSourceRange();
			ASTNode node = program.getElementAt(range.getOffset());

			if (!(node instanceof ClassDeclaration)) {
				return null;				
			}
			
			ClassDeclaration clazz = (ClassDeclaration) node;
			clazz.getBody().statements().add(method);
			
			
			
			TextEdit edits = program.rewrite(document, options);
			edits.apply(document);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupOptions() {

		options = new HashMap(PHPCorePlugin.getOptions());
		
		IScopeContext[] contents = new IScopeContext[] {
				new ProjectScope(field
						.getScriptProject()
						.getProject()),
						InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		for (int i = 0; i < contents.length; i++) {
			IScopeContext scopeContext = contents[i];
			IEclipsePreferences inode = scopeContext
					.getNode(PHPCorePlugin.ID);
			if (inode != null) {
				if (!options
						.containsKey(PHPCoreConstants.FORMATTER_USE_TABS)) {
					String useTabs = inode
							.get(PHPCoreConstants.FORMATTER_USE_TABS,
									null);
					if (useTabs != null) {
						options.put(
								PHPCoreConstants.FORMATTER_USE_TABS,
								useTabs);
					}
				}
				if (!options
						.containsKey(PHPCoreConstants.FORMATTER_INDENTATION_SIZE)) {
					String size = inode
							.get(PHPCoreConstants.FORMATTER_INDENTATION_SIZE,
									null);
					if (size != null) {
						options.put(
								PHPCoreConstants.FORMATTER_INDENTATION_SIZE,
								size);
					}
				}
			}
		}
	}
	
	
	private void initialize(ExecutionEvent event, IModelElement element) throws InvalidElementException {
		
		field = (SourceField) element;
		
		if (field == null)
			throw new InvalidElementException();
		
		type = (SourceType) field.getParent();
		
		if (type == null)
			throw new InvalidElementException();
		
		editorPart = HandlerUtil.getActiveEditor(event);
		textEditor = null;
		if (editorPart instanceof PHPStructuredEditor)
			textEditor = (PHPStructuredEditor) editorPart;
		else {
			Object o = editorPart.getAdapter(ITextEditor.class);
			if (o != null)
				textEditor = (PHPStructuredEditor) o;
		}
		
		document = textEditor.getDocument();
		
		setupOptions();

	}
	
	
	private List<SourceField> getFields() throws ModelException {
		
		List<SourceField> fields = new ArrayList<SourceField>();
		
		for (IField field : type.getFields()) {			
			fields.add((SourceField) field);			
		}
		
		
		return fields;
		
	}
	
	private List<IMethod> getMethods() throws ModelException {
		
		
		List<IMethod> methods = new ArrayList<IMethod>();
		
		for (IMethod method : type.getMethods())
			methods.add(method);
				
		return methods;
	}
	
	private class InvalidElementException extends Exception {

		private static final long serialVersionUID = 1L;
		
	}
}