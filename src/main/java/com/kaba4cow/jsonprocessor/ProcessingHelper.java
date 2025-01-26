package com.kaba4cow.jsonprocessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class ProcessingHelper {

	private ProcessingHelper() {}

	static FieldType getFieldType(Class<?> type) {
		if (Collection.class.isAssignableFrom(type))
			return FieldType.COLLECTION;
		else if (Map.class.isAssignableFrom(type))
			return FieldType.MAP;
		else
			return FieldType.OBJECT;
	}

	static List<FieldData<?, ?>> getProcessableFields(Class<?> type) throws JSONProcessorException {
		Field[] declared = type.getDeclaredFields();
		List<FieldData<?, ?>> processable = new ArrayList<>();
		for (Field field : declared)
			if (field.isAnnotationPresent(JSONField.class))
				processable.add(new FieldData<>(field, field.getAnnotation(JSONField.class)));
		return processable;
	}

	static boolean hasProcessableFields(Class<?> type) throws JSONProcessorException {
		return !getProcessableFields(type).isEmpty();
	}

	static enum FieldType {

		OBJECT, COLLECTION, MAP;

	}

	static class FieldData<T, R> {

		private final Field field;
		private final JSONField annotation;
		private final JSONValueMapper<T, R> mapper;

		@SuppressWarnings("unchecked")
		FieldData(Field field, JSONField annotation) throws JSONProcessorException {
			field.setAccessible(true);
			if (Modifier.isStatic(field.getModifiers()))
				fieldValidationException(field, "static");
			else if (Modifier.isFinal(field.getModifiers()))
				fieldValidationException(field, "final");
			this.field = field;
			this.annotation = annotation;
			try {
				this.mapper = annotation.mapper().newInstance();
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not instantiate field mapper %s for field %s",
						annotation.mapper(), field.getName());
			}
		}

		private static void fieldValidationException(Field field, String cause) throws JSONProcessorException {
			throw new JSONProcessorException("Field %s in class %s is %s and cannot be processed", field.getName(),
					field.getDeclaringClass().getName(), cause);
		}

		Class<?>[] parameterizedTypes() {
			ParameterizedType type = (ParameterizedType) field.getGenericType();
			Class<?>[] types = new Class<?>[type.getActualTypeArguments().length];
			for (int i = 0; i < types.length; i++)
				types[i] = (Class<?>) type.getActualTypeArguments()[i];
			return types;
		}

		Field field() {
			return field;
		}

		Class<?> type() {
			return field.getType();
		}

		String name() {
			return field.getName();
		}

		boolean nullable() {
			return annotation.nullable();
		}

		EnumType enumType() {
			return annotation.enumType();
		}

		@SuppressWarnings("rawtypes")
		Class<? extends Map> mapImpl() {
			return annotation.mapImpl();
		}

		@SuppressWarnings("rawtypes")
		Class<? extends Collection> collectionImpl() {
			return annotation.collectionImpl();
		}

		JSONValueMapper<T, R> mapper() {
			return mapper;
		}

	}

}
