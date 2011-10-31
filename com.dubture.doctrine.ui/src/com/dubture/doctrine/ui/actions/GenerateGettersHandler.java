package com.dubture.doctrine.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.php.internal.core.PHPCoreConstants;
import org.eclipse.php.internal.core.PHPCorePlugin;
import org.eclipse.php.internal.core.ast.nodes.AST;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org.eclipse.php.internal.core.ast.nodes.FieldAccess;
import org.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.nodes.ReturnStatement;
import org.eclipse.php.internal.core.ast.nodes.Statement;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.ui.actions.SelectionHandler;
import org.eclipse.php.internal.ui.editor.PHPStructuredEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import com.dubture.doctrine.core.util.GetterSetterUtil;
import com.dubture.doctrine.ui.dialog.GetterSetterDialog;
import com.dubture.doctrine.ui.templates.CodeGeneration;

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
	private Map options;
	private SourceType type;
	private String lineDelim;

	
	
	@SuppressWarnings("rawtypes")
	private static class GetterSetterContentProvider implements ITreeContentProvider {

		
		private Map fields;

		private static final Object[] EMPTY = new Object[0];
		
		public GetterSetterContentProvider(Map fields) {

			this.fields = fields;
		}

		@Override
		public void dispose() {
			fields.clear();
			fields = null;			
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
			
		}

		@Override
		public Object[] getElements(Object inputElement) {

			return fields.keySet().toArray();

		}

		@Override
		public Object[] getChildren(Object parentElement) {
			
			if (parentElement instanceof IField) {
				return (Object[]) fields.get(parentElement);
			}
			return EMPTY;
		}

		@Override
		public Object getParent(Object element) {

			if (element instanceof IMember) {
				return ((IMember) element).getDeclaringType();
			}
			if (element instanceof GetterSetterEntry)
				return ((GetterSetterEntry) element).field;
			
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
 
			return getChildren(element).length > 0;

		}
		
		
	}
	
	private class GetterSetterLabelProvider extends LabelProvider {

		
		@Override
		public Image getImage(Object element) {
			
			if (element instanceof GetterSetterEntry) {				
				return DLTKUIPlugin.getImageDescriptorRegistry().get(DLTKPluginImages.DESC_FIELD_PUBLIC);
			} else if (element instanceof IField){
				return DLTKUIPlugin.getImageDescriptorRegistry().get(DLTKPluginImages.DESC_FIELD_PRIVATE);
				
			}
			return super.getImage(element);

		}
		@Override
		public String getText(Object element) {

			if (element instanceof GetterSetterEntry) {
				GetterSetterEntry entry = (GetterSetterEntry) element;
				return entry.getName();
			} else if (element instanceof IField){
				return GetterSetterUtil.getFieldName(((IField)element));
				
			}
			return super.getText(element);
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IModelElement element = getCurrentModelElement(event);
		
		if (element == null) {			
			return null;
		}
		
		if (!(element instanceof SourceType)) {			
			while(element.getParent() != null) {				
				element = element.getParent();				
				if (element instanceof SourceType) {
					break;
				}
			}
		}
		
		if (element == null || !(element instanceof SourceType)) {
			return null;
		}
		
		type = (SourceType) element;
		
		try {

			if (type.getFields().length == 0)
				return null;

			initialize(event, element);
			
			Map fields = getFields();			
			final Shell p = DLTKUIPlugin.getActiveWorkbenchShell();
			GetterSetterContentProvider cp = new GetterSetterContentProvider(fields);
			GetterSetterDialog dialog = new GetterSetterDialog(p, new GetterSetterLabelProvider(), cp, type);
			
			dialog.setContainerMode(true);
			dialog.setInput(type);
			dialog.setTitle("Generate Getters and Setters");
			
			if (dialog.open() == Window.OK) {
				
				List<GetterSetterEntry> entries = new ArrayList<GetterSetterEntry>();				
				Object[] dialogResult = dialog.getResult();
				
				for (Object o : dialogResult) {					
					if (o instanceof GetterSetterEntry) {				
						entries.add((GetterSetterEntry) o);						
					}
				}
				
				generate(entries, dialog.getModifier());
				
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
	private void generate(final List<GetterSetterEntry> entries, final int modifier) throws Exception {

		ISourceModule source = type.getSourceModule();			
		String name = type.getElementName().replace("$", "");			
		StringBuffer buffer = new StringBuffer(name);			
		buffer.replace(0, 1, Character.toString(Character.toUpperCase(name.charAt(0))));			
		name = buffer.toString();			
					
		ASTParser parser = ASTParser.newParser(source);
		parser.setSource(document.get().toCharArray());
		
		Program program = parser.createAST(new NullProgressMonitor());			
		program.recordModifications();		
		AST ast = program.getAST();		

		ISourceRange range = type.getSourceRange();		
		ASTNode node = program.getElementAt(range.getOffset());

		if (!(node instanceof ClassDeclaration)) {
			return ;				
		}
		
		ClassDeclaration clazz = (ClassDeclaration) node;
		Block body = clazz.getBody();
		
		List<Statement> bodyStatements = body.statements();
		
		int end = bodyStatements.get(bodyStatements.size()-1).getEnd();

		lineDelim = TextUtilities.getDefaultLineDelimiter(document);
		
		for (GetterSetterEntry entry : entries) {
			
			List<FormalParameter> params = new ArrayList<FormalParameter>();			
			List<Statement> statements = new ArrayList<Statement>();
			
			Variable left = ast.newVariable("this");
			Variable right = ast.newVariable(entry.getRawFieldName());
			right.setIsDollared(false);
			FieldAccess access = ast.newFieldAccess(left, right);

			
			if (!entry.isGetter) {
				
				
				String type = entry.getType();				
				Identifier paramID = null;
				
				if (type != null)
					paramID = ast.newIdentifier(type);
 				
				Variable var = ast.newVariable(entry.getRawFieldName());
				FormalParameter param = ast.newFormalParameter(paramID, var, null, true);
				params.add(param);
				
				Assignment assignment = ast.newAssignment();
				
				assignment.setLeftHandSide(access);
				assignment.setOperator(Assignment.OP_EQUAL);
				assignment.setRightHandSide(ast.newVariable(entry.getRawFieldName()));
								
				ExpressionStatement statement = ast.newExpressionStatement(assignment);
				statements.add(statement);
				
				
			} else {
				
				ReturnStatement returnStatement = ast.newReturnStatement(access);
				statements.add(returnStatement);
				
			}
			
			Block block = ast.newBlock(statements);
			
			String identifier = entry.getIdentifier();
			Identifier methodIdentifier = ast.newIdentifier(identifier);
			FunctionDeclaration function = ast.newFunctionDeclaration(methodIdentifier, params, block, false);
			
			MethodDeclaration method = ast.newMethodDeclaration(modifier, function);			
			body.statements().add(method);
			
			StringBuffer b = new StringBuffer();
			String tab = "\\t"; 
			method.toString(b, tab);
			
			
			
			
//			String generated = CodeGeneration.getGetterMethodBodyContent(type.getScriptProject(), type.getElementName(), access.toString(), entry.getName(), lineDelim);
//						
//
//			document.replace(end, 0, generated);
			
//			System.err.println(generated);
			
			
		}
		
		String code = CodeGeneration.getMethodBodyContent(true, type.getScriptProject(), type.getElementName(), "some", "statement", lineDelim);
		
		System.err.println("code:");
		System.err.println(code);
		
		
//		TextEdit edits = program.rewrite(document, options);
//		edits.apply(document);		
		
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupOptions() {

		options = new HashMap(PHPCorePlugin.getOptions());
		
		IScopeContext[] contents = new IScopeContext[] {
				new ProjectScope(type
						.getScriptProject()
						.getProject()),
						InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		
		for (int i = 0; i < contents.length; i++) {
			
			IScopeContext scopeContext = contents[i];
			IEclipsePreferences inode = scopeContext.getNode(PHPCorePlugin.ID);
			
			if (inode != null) {
				
				if (!options.containsKey(PHPCoreConstants.FORMATTER_USE_TABS)) {
					
					String useTabs = inode.get(PHPCoreConstants.FORMATTER_USE_TABS,null);
					if (useTabs != null) {
						options.put(PHPCoreConstants.FORMATTER_USE_TABS, useTabs);
					}
				}
				
				if (!options.containsKey(PHPCoreConstants.FORMATTER_INDENTATION_SIZE)) {
					
					String size = inode.get(PHPCoreConstants.FORMATTER_INDENTATION_SIZE,null);
					
					if (size != null) {
						options.put(PHPCoreConstants.FORMATTER_INDENTATION_SIZE,size);
					}
				}
			}
		}
	}
	
	
	private void initialize(ExecutionEvent event, IModelElement element) throws InvalidElementException {
		
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
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getFields() throws ModelException {
		
		IField[] fields = type.getFields();
		Map result = new LinkedHashMap();
		for (int i = 0; i < fields.length; i++) {
			IField field = fields[i];
			List l = new ArrayList(2);

			l.add(new GetterSetterEntry(field, true));
			l.add(new GetterSetterEntry(field, false));

			if (!l.isEmpty())
				result.put(field, l.toArray(new GetterSetterEntry[l.size()]));

		}

		return result;
		
	}
	
	private class InvalidElementException extends Exception {

		private static final long serialVersionUID = 1L;
		
	}
	
	public static class GetterSetterEntry {
		
		
		public final IField field;
		public final boolean isGetter;
		private String name = null;
		private String identifier = null;
		private String type = null;
		private String raw = null;
		
		public GetterSetterEntry(IField field, boolean isGetterEntry) {
		
			this.field = field;
			this.isGetter = isGetterEntry;
		}
		
		public String getIdentifier() {
			
			if (identifier != null)
				return identifier;
			
			if (isGetter)
				return identifier = GetterSetterUtil.getGetterName(field);
			else return identifier = GetterSetterUtil.getSetterName(field);
			
		}
		
		public String getType() {

			if (type != null)
				return type;
			
			return type = GetterSetterUtil.getTypeReference(field);
			
		}
		
		public String getName() {

			if (name != null)
				return name;
			
			if (isGetter) {			
				return name = String.format("%s()", getIdentifier());
			} else {
				return name = String.format("%s(%s)", getIdentifier(), getType());				
			}
		}


		public String getRawFieldName() {

			if (raw != null)
				return raw;
			
			return raw = field.getElementName().replace("$", "");
			
		}
		
	}
}