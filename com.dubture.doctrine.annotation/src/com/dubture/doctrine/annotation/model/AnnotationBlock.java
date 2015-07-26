package com.dubture.doctrine.annotation.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * This class holds annotaion list for one block (comment)
 * 
 * @author zulus
 */
public class AnnotationBlock extends AnnotationSourceElement {
	private List<Annotation> annotations;
	
	public AnnotationBlock() {
		annotations = new LinkedList<Annotation>();
	}
	
	public AnnotationBlock(List<Annotation> annotations) {
		this.annotations = annotations;
	}
	
	public void addAnnotation(Annotation annotation) {
		annotations.add(annotation);
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}
	
	public List<Annotation> findAnnotations(String className) {
		List<Annotation> result = new LinkedList<Annotation>();
		for (Annotation a :annotations) {
			if (a.getClassName().equalsIgnoreCase(className)) {
				result.add(a);
			}
		}
		
		return result;
	}
	
	public boolean isEmpty() {
		return annotations.isEmpty();
	}

	@Override
	public void traverse(AnnotationVisitor visitor) {
		if (visitor.visit(this)) {
			for (Annotation a : annotations) {
				a.traverse(visitor);
			}
		}
		visitor.endVisit(this);
	}
}
