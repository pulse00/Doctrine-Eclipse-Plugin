package com.dubture.doctrine.annotation.model;

/**
 * This represents an {@link Annotation} argument. The argument
 * value is an {@link IArgumentValue}.
 *
 * @author Matthieu Vachon <matthieu.o.vachon@gmail.com>
 */
public class Argument extends AnnotationSourceElement {

    protected IArgumentValue value;

    public Argument(IArgumentValue value){
        this.value = value;
    }

    public IArgumentValue getValue() {
        return value;
    }

    public String getValueAsString() {
        return value.toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }

	@Override
	public void traverse(AnnotationVisitor visitor) {
		if (visitor.visit(this)) {
			value.traverse(visitor);;
		}
		visitor.endVisit(this);
	}
}
