package com.dubture.doctrine.annotation.model;

/**
 * This object represents a boolean value.
 *
 * @author Matthieu Vachon <matthieu.o.vachon@gmail.com>
 */
public class BooleanValue extends ArgumentValue {

	private Boolean value;

	public BooleanValue() {
		value = false;
	}

	public BooleanValue(boolean value) {
		this.value = Boolean.valueOf(value);
	}

	public BooleanValue(String value) {
		this.value = Boolean.parseBoolean(value);
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public ArgumentValueType getType() {
		return ArgumentValueType.BOOLEAN;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public void traverse(AnnotationVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
