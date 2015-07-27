package com.dubture.doctrine.internal.core.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.core.compiler.ast.nodes.NamespaceReference;
import org.eclipse.php.internal.core.documentModel.parser.PHPRegionContext;
import org.eclipse.php.internal.core.documentModel.parser.regions.IPhpScriptRegion;
import org.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionCollection;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionContainer;

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
		String region = getPHPRegionType(document, offset);
		return PHPRegionTypes.PHPDOC_COMMENT.equals(region) || PHPRegionTypes.PHPDOC_PARAM.equals(region) || PHPRegionTypes.PHPDOC_DESC.equals(region);
	}

	public static String getAnnotationName(IDocument document, int offset) throws BadLocationException {
		return getAnnotationName(document, offset, document.getLength() - 1);
	}

	public static String getAnnotationName(IDocument document, int offset, int max) throws BadLocationException {
		if (document.getLength() < offset) {
			return null;
		}
		StringBuilder name = new StringBuilder();
		for (int i = offset - 1; i >= 0; i--) {
			char ch = document.getChar(i);

			if (ch == '@') {
				break;
			} else if (!isIdentPart(ch)) {
				return null;
			}
			name.insert(0, ch);
		}
		for (int i = offset; i <= max; i++) {
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

	public static boolean isInsideAnnotation(String text, int offset) {
		int end = offset;
		if (offset >= text.length()) {
			offset = text.length() - 1;
		}
		for (; end > 0 && text.charAt(end) != '@'; end--) {
		}
		end--;
		boolean inString = false;
		boolean go = false;
		int calls = 0;
		for (; !go && end > 1; end--) {
			char ch = text.charAt(end);
			if (inString && ch == '"' && text.charAt(end - 1) != '\\') {
				inString = false;
				continue;
			}
			switch (ch) {
			case '"':
				inString = true;
				continue;
			case ')':
				calls ++;
				continue;
			case '(':
				if (calls == 0) {
					go = true;
					break;
				}
				calls--;
			}
		}
		end--;
		if (end == 1) {
			return false;
		}
		boolean noMoreWhiteSpace = false;
		for (; end > 1; end--) {
			char ch = text.charAt(end);
			if (isIdentPart(ch)) {
				noMoreWhiteSpace = true;
				continue;
			}
			if (!noMoreWhiteSpace && (Character.isWhitespace(ch) || ch == '*')) {
				continue;
			} else if (ch == '@') {
				return true;
			}
			return false;
		}
		
		return false;
	}
}
