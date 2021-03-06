package com.dubture.doctrine.annotation.model;

/**
 * This object represents a null value.
 *
 * @author Matthieu Vachon <matthieu.o.vachon@gmail.com>
 */
public final class NullValue extends ArgumentValue {

	public NullValue() {

	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public ArgumentValueType getType() {
		return ArgumentValueType.NULL;
	}

	@Override
	public String toString() {
		return "null";
	}

	@Override
	public void traverse(AnnotationVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
