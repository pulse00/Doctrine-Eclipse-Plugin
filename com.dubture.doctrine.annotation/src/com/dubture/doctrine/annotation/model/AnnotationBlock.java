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
}
