package com.dubture.doctrine.ui.templates;

import java.io.IOException;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.corext.util.Strings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContext;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;

import com.dubture.doctrine.ui.DoctrineUIPlugin;

@SuppressWarnings("restriction")
public class StubUtility {

	
	public static String getGetterMethodBodyContent(IScriptProject project,
			String destTypeName, String methodName, String fieldName,
			String lineDelimiter) throws CoreException {
		String templateName = CodeTemplateContextType.GETTERSTUB_ID;
		Template template = getCodeTemplate(templateName, project);
		if (template == null) {
			return null;
		}
		CodeTemplateContext context = new CodeTemplateContext(template
				.getContextTypeId(), project, lineDelimiter);
		context.setVariable(CodeTemplateContextType.ENCLOSING_METHOD,
				methodName);
		context.setVariable(CodeTemplateContextType.ENCLOSING_TYPE,
				destTypeName);
		context.setVariable(CodeTemplateContextType.FIELD, fieldName);

		return evaluateTemplate(context, template);
	}
	
	private static Template getCodeTemplate(String id, IScriptProject sp) {
		if (sp == null)
			return DoctrineUIPlugin.getDefault().getCodeTemplateStore()
					.findTemplateById(id);
		ProjectTemplateStore projectStore = new ProjectTemplateStore(sp
				.getProject());
		try {
			projectStore.load();
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		return projectStore.findTemplateById(id);
	}
	
	private static String evaluateTemplate(CodeTemplateContext context,
			Template template) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer = context.evaluate(template);
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
		if (buffer == null)
			return null;
		String str = buffer.getString();
		if (Strings.containsOnlyWhitespaces(str)) {
			return null;
		}
		return str;
	}

	private static String evaluateTemplate(CodeTemplateContext context,
			Template template, String[] fullLineVariables) throws CoreException {
		TemplateBuffer buffer;
		try {
			buffer = context.evaluate(template);
			if (buffer == null)
				return null;
			String str = fixEmptyVariables(buffer, fullLineVariables);
			if (Strings.containsOnlyWhitespaces(str)) {
				return null;
			}
			return str;
		} catch (BadLocationException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		} catch (TemplateException e) {
			throw new CoreException(Status.CANCEL_STATUS);
		}
	}
	

	// remove lines for empty variables
	private static String fixEmptyVariables(TemplateBuffer buffer,
			String[] variables) throws MalformedTreeException,
			BadLocationException {
		IDocument doc = new Document(buffer.getString());
		int nLines = doc.getNumberOfLines();
		MultiTextEdit edit = new MultiTextEdit();
		HashSet<Integer> removedLines = new HashSet<Integer>();
		for (int i = 0; i < variables.length; i++) {
			TemplateVariable position = findVariable(buffer, variables[i]); // look
			// if
			// Javadoc
			// tags
			// have
			// to
			// be
			// added
			if (position == null || position.getLength() > 0) {
				continue;
			}
			int[] offsets = position.getOffsets();
			for (int k = 0; k < offsets.length; k++) {
				int line = doc.getLineOfOffset(offsets[k]);
				IRegion lineInfo = doc.getLineInformation(line);
				int offset = lineInfo.getOffset();
				String str = doc.get(offset, lineInfo.getLength());
				if (Strings.containsOnlyWhitespaces(str) && nLines > line + 1
						&& removedLines.add(new Integer(line))) {
					int nextStart = doc.getLineOffset(line + 1);
					edit.addChild(new DeleteEdit(offset, nextStart - offset));
				}
			}
		}
		edit.apply(doc, 0);
		return doc.get();
	}
	
	private static TemplateVariable findVariable(TemplateBuffer buffer,
			String variable) {
		TemplateVariable[] positions = buffer.getVariables();
		for (int i = 0; i < positions.length; i++) {
			TemplateVariable curr = positions[i];
			if (variable.equals(curr.getType())) {
				return curr;
			}
		}
		return null;
	}
	


}
