package com.dubture.doctrine.annotation.model;

/**
 * @author zulus
 */
public abstract class AnnotationVisitor {
	
	public boolean visit(Annotation node) {
		return true;
	}
	
	public boolean visit(AnnotationBlock node) {
		return true;
	}
	public boolean visit(AnnotationClass node) {
		return true;
	}
	public boolean visit(AnnotationDeclaration node) {
		return true;
	}
	public boolean visit(AnnotationValue node) {
		return true;
	}
	public boolean visit(Argument node) {
		return true;
	}
	public boolean visit(ArgumentValue node) {
		switch (node.getType()) {
		case ARRAY:
			return visit((ArrayValue) node);
		case NULL:
			return visit((NullValue) node);
		case BOOLEAN:
			return visit((BooleanValue) node);
		case NUMBER:
			return visit((NumberValue) node);
		case OBJECT:
			return visit((ObjectValue) node);
		case STRING:
			return visit((StringValue) node);
		default:
			return true;
		}
	}
	public boolean visit(ArrayValue node) {
		return true;
	}
	public boolean visit(BooleanValue node) {
		return true;
	}
	public boolean visit(NamedArgument node) {
		return true;
	}
	public boolean visit(NullValue node) {
		return true;
	}
	public boolean visit(NumberValue node) {
		return true;
	}
	public boolean visit(ObjectValue node) {
		return true;
	}
	public boolean visit(StringValue node) {
		return true;
	}
	
	public void endVisit(Annotation node) {
	}
	
	public void endVisit(AnnotationBlock node) {
	}
	public void endVisit(AnnotationClass node) {
	}
	public void endVisit(AnnotationDeclaration node) {
	}
	public void endVisit(AnnotationValue node) {
	}
	public void endVisit(Argument node) {
	}
	public void endVisit(ArgumentValue node) {
		switch (node.getType()) {
		case ARRAY:
			endVisit((ArrayValue) node);
		case NULL:
			endVisit((NullValue) node);
		case BOOLEAN:
			endVisit((BooleanValue) node);
		case NUMBER:
			endVisit((NumberValue) node);
		case OBJECT:
			endVisit((ObjectValue) node);
		case STRING:
			endVisit((StringValue) node);
		default:
		}
	}
	public void endVisit(ArrayValue node) {
	}
	public void endVisit(BooleanValue node) {
	}
	public void endVisit(NamedArgument node) {
	}
	public void endVisit(NullValue node) {
	}
	public void endVisit(NumberValue node) {
	}
	public void endVisit(ObjectValue node) {
	}
	public void endVisit(StringValue node) {
	}
}
