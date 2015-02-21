package com.dubture.doctrine.core.compiler;

import org.eclipse.php.core.compiler.IPHPModifiers;

public interface IDoctrineModifiers extends IPHPModifiers {
	public static final int NonDoctrine = 1 << IPHPModifiers.USER_MODIFIER;
	
	public static final int AccEntity = (1 << IPHPModifiers.USER_MODIFIER + 1);
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetClass = (1 << IPHPModifiers.USER_MODIFIER + 2);
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetField = (1 << IPHPModifiers.USER_MODIFIER + 3);
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetMethod = (1 << IPHPModifiers.USER_MODIFIER + 4);
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetAnnotation = (1 << IPHPModifiers.USER_MODIFIER + 5);
	
	public static final int USER_MODIFIER = IPHPModifiers.USER_MODIFIER + 6;
}
