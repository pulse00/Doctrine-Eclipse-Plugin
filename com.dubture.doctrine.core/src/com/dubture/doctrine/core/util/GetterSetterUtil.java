package com.dubture.doctrine.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTag;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPFieldDeclaration;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPModuleDeclaration;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;

import com.dubture.doctrine.core.log.Logger;

/**
 * 
 * Utility class for getter/setter generation.
 * 
 * 
 * @author Robert Gruendler <r.gruendler@gmail.com>
 *
 */
@SuppressWarnings("restriction")
public class GetterSetterUtil {
	
	public static final String GET = "get";
	public static final String SET = "set";	
	private static final String[] bin = {"bool", "int", "integer", "string"};	
	private static final List<String> builtin = new ArrayList<String>(Arrays.asList(bin));
	
	private static class FieldReferenceParser extends PHPASTVisitor {
		
		private final IField field;
		
		private SimpleReference reference = null;
		
		public FieldReferenceParser(IField field) {
			
			this.field = field;			
		}

		public boolean visit(PHPFieldDeclaration s) throws Exception {

			if (s.getName().equals(field.getElementName())) {
				PHPDocBlock doc = s.getPHPDoc();
				if (doc != null) {
					if (doc.getTags().length == 1) {
						PHPDocTag[] tags = doc.getTags();
						if (tags[0].getReferences().length == 1) {
							SimpleReference[] refs = tags[0].getReferences();
							if (refs.length == 1) {							
								reference = refs[0];
							}
						}
					}
				}				
				return false;
			}
			return true;
		}
		
		public SimpleReference getReference() {
			
			return reference;
		}
	}
	
	public static String getTypeReference(final IField field) {

		String type = null;
		
		try {
			
			PHPModuleDeclaration module = (PHPModuleDeclaration) SourceParserUtil.parse(field.getSourceModule(), null);					
			FieldReferenceParser typeParser = new FieldReferenceParser(field);
			module.traverse(typeParser);
			
			if (typeParser.getReference() != null) {			
				SimpleReference ref = typeParser.getReference();			
				type = ref.getName();
				
				if (builtin.contains(type)) {
					type = "";
				}
			}
			
		} catch (Exception e1) {
			Logger.logException(e1);
		}
		
		return type;
		
	}
	

	public static String getSetterName(final IField field) {

		String name = prepareField(field);
		return SET + name;
				

	}
	
	public static String getGetterName(IField field) {
		
		String name = prepareField(field);			
		return GET + name;				
		
	}
	
	private static String prepareField(IField field) {
		
		String name = field.getElementName().replace("$", "");			
		StringBuffer buffer = new StringBuffer(name);			
		buffer.replace(0, 1, Character.toString(Character.toUpperCase(name.charAt(0))));			
		return buffer.toString();		
		
	}

	public static String getFieldName(IField iField) {

		Assert.isNotNull(iField);		
		return iField.getElementName().replace("$", "");
		
	}

}
