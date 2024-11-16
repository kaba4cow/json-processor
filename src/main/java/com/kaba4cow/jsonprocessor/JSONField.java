package com.kaba4cow.jsonprocessor;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Annotation used to mark fields that should be processed during JSON serialization/deserialization. This annotation can only
 * be applied to non-static, and non-final fields.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface JSONField {

	/**
	 * Indicates whether the field can be set to {@code null} during deserialization.
	 *
	 * @return {@code true} if the field can be null, {@code false} otherwise
	 */
	public boolean nullable() default false;

	/**
	 * Defines how enum fields should be serialized or deserialized.
	 *
	 * @return the {@link EnumType} to use for processing enum fields
	 */
	public EnumType enumType() default EnumType.STRING;

	/**
	 * Specifies a custom {@link JSONValueMapper} to use for this field during serialization and deserialization.
	 *
	 * @return the class of the custom {@code JSONValueMapper}
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends JSONValueMapper> mapper() default JSONValueMapper.Default.class;

	/**
	 * Specifies an implementation for map fields which is used if the field type is an interface, abstract class or a
	 * non-static inner class. The implementation must have an accessible no-argument public constructor.
	 *
	 * @return the implementation of {@link Map}.
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends Map> mapImpl() default HashMap.class;

	/**
	 * Specifies an implementation for collection fields which is used if the field type is an interface, abstract class or a
	 * non-static inner class. The implementation must have an accessible no-argument public constructor.
	 *
	 * @return the implementation of {@link Collection}.
	 */
	@SuppressWarnings("rawtypes")
	public Class<? extends Collection> collectionImpl() default ArrayList.class;

	/**
	 * Enumeration for specifying how enum fields should be serialized or deserialized.
	 */
	public static enum EnumType {

		/**
		 * Serialize or deserialize enum fields as their string names.
		 */
		STRING,

		/**
		 * Serialize or deserialize enum fields as their ordinal values.
		 */
		ORDINAL;

	}

}
