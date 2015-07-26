package com.dubture.doctrine.annotation.model;

/**
 * This is an {@link AnnotationValue}. It is used mainly to distinguish
 * main annotation from inner ones. An inner annotation will be
 * an {@link AnnotationValue}. However, a main annotation will only
 * be an {@link Annotation}.
 *
 * @author Matthieu Vachon <matthieu.o.vachon@gmail.com>
 */
public class AnnotationValue extends Annotation implements IArgumentValue {
    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public ArgumentValueType getType() {
        return ArgumentValueType.ANNOTATION;
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
    @Override
    public void traverse(AnnotationVisitor visitor) {
    	if (visitor.visit(this)) {
			this.annotationClass.traverse(visitor);
			this.annotationDeclaration.traverse(visitor);
		}
		visitor.endVisit(this);
    }
}
