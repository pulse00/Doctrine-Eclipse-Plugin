package com.dubture.doctrine.core.compiler;

import org.eclipse.php.core.compiler.IPHPModifiers;

public interface IDoctrineModifiers extends IPHPModifiers {
	public static final int NonDoctrine = 1 << IPHPModifiers.USER_MODIFIER;
	
	public static final int AccEntity = (1 << IPHPModifiers.USER_MODIFIER + 1);
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetClass = IPHPModifiers.AccUpVar; // int end, switch to unused flag for this context
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetField = IPHPModifiers.AccVariadic; // int end, switch to unused flag for this context
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetMethod = IPHPModifiers.AccNullable; // int end, switch to unused flag for this context
	
	/**
	 * @since 1.0.8
	 */
	public static final int AccTargetAnnotation =IPHPModifiers.AccReturn; // int end, switch to unused flag for this context
	
	public static final int USER_MODIFIER = IPHPModifiers.USER_MODIFIER + 6;
}
