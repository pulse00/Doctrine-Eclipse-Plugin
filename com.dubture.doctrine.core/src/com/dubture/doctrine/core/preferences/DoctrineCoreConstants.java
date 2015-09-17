/*******************************************************************************
 * This file is part of the doctrine eclipse plugin.
 *
 * (c) Dawid Pakuła <zulus@w3des.net>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.doctrine.core.preferences;

import com.dubture.doctrine.core.DoctrineCorePlugin;


public class DoctrineCoreConstants {
	public static final String PLUGIN_ID = DoctrineCorePlugin.ID;
	
	public static final String INDEX_VERSION = "0.0.7";
	public static final String INDEX_VERSION_PREFERENCE = "indexVersion";
	
	public static final String DEFAULT_ANNOTATION_NAMESPACE = "Doctrine\\Common\\Annotations\\Annotation";
	public static final String ANNOTATION_TAG_NAME = "Annotation";
	public static final String ENUM_ANNOTATION = "Doctrine\\Common\\Annotations\\Annotation\\Enum";
	public static final String TARGET_ANNOTATION = "Doctrine\\Common\\Annotations\\Annotation\\Target";
	public static final String COLUMN_ANNOTATION = "Doctrine\\ORM\\Mapping\\Column";
	public static final String DEFAULT_FIELD = "value";
}
