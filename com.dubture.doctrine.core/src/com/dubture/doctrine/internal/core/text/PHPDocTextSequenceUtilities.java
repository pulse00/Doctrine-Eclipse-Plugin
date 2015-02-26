package com.dubture.doctrine.internal.core.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.documentModel.parser.PHPRegionContext;
import org.eclipse.php.internal.core.documentModel.parser.regions.IPhpScriptRegion;
import org.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionContainer;

import com.dubture.doctrine.core.log.Logger;

/**
 * @author zulus
 */
@SuppressWarnings("restriction")
public class PHPDocTextSequenceUtilities {
	public static String getPHPRegionType(IStructuredDocument document, int offset) {
		if (document == null) {
			return null;
		}

		IStructuredDocumentRegion sRegion = document.getRegionAtCharacterOffset(offset);

		if (sRegion == null) {
			return null;
		}

		ITextRegion tRegion = sRegion.getRegionAtCharacterOffset(offset);

		ITextRegionCollection container = sRegion;
		if (tRegion instanceof ITextRegionContainer) {
			container = (ITextRegionContainer) tRegion;
			tRegion = container.getRegionAtCharacterOffset(offset);
		}
		if (tRegion == null || tRegion.getType() != PHPRegionContext.PHP_CONTENT) {
			return null;
		}
		IPhpScriptRegion phpScriptRegion = (IPhpScriptRegion) tRegion;
		try {
			tRegion = phpScriptRegion.getPhpToken(offset - container.getStartOffset() - phpScriptRegion.getStart());
		} catch (BadLocationException e) {
		}
		
		if (tRegion == null) {
			return null;
		}
		
		return tRegion.getType();
	}
	
	public static boolean isPHPDoc(IStructuredDocument document, int offset) {
		return PHPRegionTypes.PHPDOC_COMMENT.equals(getPHPRegionType(document, offset));
	}
	
	public static String getAnnotationName(IDocument document, int offset) throws BadLocationException {
		return getAnnotationName(document, offset, document.getLength() - 1);
	}
	
	public static String getAnnotationName(IDocument document, int offset, int max) throws BadLocationException {
		if (document.getLength() < offset) {
			return null;
		}
		StringBuilder name = new StringBuilder();
		for (int i = offset - 1; i >=0; i--) {
			char ch = document.getChar(i);
			
			if (ch == '@') {
				break;
			} else if (!isIdentPart(ch)) {
				return null;
			}
			name.insert(0, ch);
		}
		for (int i = offset; i <= max ; i++) {
			char ch = document.getChar(i);
			if (Character.isWhitespace(ch) || ch == '(') {
				break;
			} else if (!isIdentPart(ch)) {
				return null; // broken code
			}
			name.append(ch);
		}
		
		return name.toString();
	}
	
	public static boolean isIdentPart(char ch) {
		return Character.isLetterOrDigit(ch) || ch == NamespaceReference.NAMESPACE_SEPARATOR || ch == '_';
	}
}
