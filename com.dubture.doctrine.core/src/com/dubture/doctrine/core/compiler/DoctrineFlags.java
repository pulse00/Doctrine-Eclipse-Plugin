package com.dubture.doctrine.core.compiler;

import org.eclipse.php.core.compiler.PHPFlags;

public class DoctrineFlags extends PHPFlags implements IDoctrineModifiers{
	public static boolean isEntity(int flags) {
		return (flags & AccEntity) != 0; 
	}
	
	public static boolean isAnnotation(int flags) {
		return isClass(flags) && (flags & AccAnnotation) != 0;
	}
}
