package com.kaba4cow.jsonprocessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kaba4cow.jsonprocessor.JSONField.EnumType;

/**
 * Utility class for serializing and deserializing Java objects to and from JSON. This class provides methods to convert objects
 * into JSON representation and vice versa.
 */
public class JSONProcessor {

	private JSONProcessor() {}

	/**
	 * Serializes the specified object into a {@link JSONObject}.
	 *
	 * @param object the object to serialize
	 * 
	 * @return a {@link JSONObject} representing the object
	 * 
	 * @throws JSONProcessorException if serialization fails
	 */
	public static JSONObject serialize(Object object) throws JSONProcessorException {
		synchronized (object) {
			return new Serializer(object).serialize();
		}
	}

	/**
	 * Serializes a collection of objects into a {@link JSONArray}.
	 *
	 * @param <T>        the type of objects in the collection
	 * @param collection the collection to serialize
	 * 
	 * @return a {@link JSONArray} representing the collection
	 * 
	 * @throws JSONProcessorException if serialization fails
	 */
	public static <T> JSONArray serializeAll(Collection<T> collection) throws JSONProcessorException {
		synchronized (collection) {
			JSONArray json = new JSONArray();
			for (T object : collection)
				json.put(serialize(object));
			return json;
		}
	}

	/**
	 * Serializes a map of objects into a {@link JSONObject}.
	 *
	 * @param <T>        the type of objects in the map
	 * @param collection the map to serialize
	 * 
	 * @return a {@link JSONObject} representing the map
	 * 
	 * @throws JSONProcessorException if serialization fails
	 */
	public static <T> JSONObject serializeAll(Map<String, T> map) throws JSONProcessorException {
		synchronized (map) {
			JSONObject json = new JSONObject();
			for (String key : map.keySet())
				json.put(key, serialize(map.get(key)));
			return json;
		}
	}

	/**
	 * Deserializes a {@link JSONObject} into an object of the specified type.
	 *
	 * @param <T>  the type of the object to deserialize
	 * @param type the class of the object
	 * @param json the {@link JSONObject} to deserialize
	 * 
	 * @return an object of the specified type
	 * 
	 * @throws JSONProcessorException if deserialization fails
	 */
	public static <T> T deserialize(Class<T> type, JSONObject json) throws JSONProcessorException {
		synchronized (json) {
			return new Deserializer<>(type, json).deserialize();
		}
	}

	/**
	 * Deserializes a {@link JSONArray} into a list of objects of the specified type.
	 *
	 * @param <T>  the type of objects in the list
	 * @param type the class of the objects
	 * @param json the {@link JSONArray} to deserialize
	 * 
	 * @return a list of objects of the specified type
	 * 
	 * @throws JSONProcessorException if deserialization fails
	 */
	public static <T> List<T> deserializeAll(Class<T> type, JSONArray json) throws JSONProcessorException {
		synchronized (json) {
			List<T> list = new ArrayList<>();
			for (int i = 0; i < json.length(); i++)
				list.add(deserialize(type, json.getJSONObject(i)));
			return list;
		}
	}

	/**
	 * Deserializes a {@link JSONObject} into a map of objects of the specified type.
	 *
	 * @param <T>  the type of objects in the map
	 * @param type the class of the objects
	 * @param json the {@link JSONObject} to deserialize
	 * 
	 * @return a map of objects of the specified type
	 * 
	 * @throws JSONProcessorException if deserialization fails
	 */
	public static <T> Map<String, T> deserializeAll(Class<T> type, JSONObject json) throws JSONProcessorException {
		synchronized (json) {
			Map<String, T> map = new HashMap<>();
			for (String key : json.keySet())
				map.put(key, deserialize(type, json.getJSONObject(key)));
			return map;
		}
	}

	private static FieldType getFieldType(Class<?> type) {
		if (Collection.class.isAssignableFrom(type))
			return FieldType.COLLECTION;
		else if (Map.class.isAssignableFrom(type))
			return FieldType.MAP;
		else
			return FieldType.OBJECT;
	}

	private static List<FieldData<?, ?>> getProcessableFields(Class<?> type) throws JSONProcessorException {
		Field[] declared = type.getDeclaredFields();
		List<FieldData<?, ?>> processable = new ArrayList<>();
		for (Field field : declared)
			if (field.isAnnotationPresent(JSONField.class))
				processable.add(new FieldData<>(field, field.getAnnotation(JSONField.class)));
		return processable;
	}

	private static boolean hasProcessableFields(Class<?> type) throws JSONProcessorException {
		return !getProcessableFields(type).isEmpty();
	}

	/**
	 * Thrown to indicate an error occurred during JSON processing operations. This includes validation errors, mapping errors,
	 * and reflection-related issues.
	 */
	public static class JSONProcessorException extends Exception {

		private static final long serialVersionUID = 1L;

		private JSONProcessorException(String format, Object... args) {
			super(String.format(format, args));
		}

		private JSONProcessorException(Throwable cause, String format, Object... args) {
			super(String.format(format, args), cause);
		}

	}

	private static class Serializer {

		private final Object serializedObject;
		private final Class<?> serializedType;
		private final List<FieldData<?, ?>> processableFields;
		private final JSONObject serializedJsonObject;

		private Serializer(Object object) throws JSONProcessorException {
			serializedObject = Objects.requireNonNull(object);
			serializedType = serializedObject.getClass();
			processableFields = getProcessableFields(serializedType);
			if (processableFields.isEmpty())
				throw new JSONProcessorException("Class %s has no mappable fields", serializedType.getName());
			serializedJsonObject = new JSONObject();
		}

		private JSONObject serialize() throws JSONProcessorException {
			try {
				for (FieldData<?, ?> field : processableFields) {
					Object value = field.field().get(serializedObject);
					if (!field.nullable())
						Objects.requireNonNull(value);
					if (Objects.nonNull(value)) {
						FieldType fieldType = getFieldType(value.getClass());
						switch (fieldType) {
							case COLLECTION:
								serializedJsonObject.put(field.name(), serializeCollection((Collection<?>) value, field));
								break;
							case MAP:
								serializedJsonObject.put(field.name(), serializeMap((Map<?, ?>) value, field));
								break;
							case OBJECT:
								serializedJsonObject.put(field.name(), serializeObject(value, field));
								break;
							default:
								throw new JSONProcessorException("Unreachable: field type %s is not supported", fieldType);
						}
					}
				}
				return serializedJsonObject;
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not serialize object of class %s", serializedType.getName());
			}
		}

		@SuppressWarnings("unchecked")
		private <S, D> Object serializeObject(Object value, FieldData<S, D> field) throws JSONProcessorException {
			if (value.getClass().isEnum())
				return serializeEnum(value, field.enumType());
			else
				try {
					return hasProcessableFields(value.getClass()) //
							? new Serializer(value).serialize() //
							: field.mapper().toJSONObject((S) value);
				} catch (Exception exception) {
					throw new JSONProcessorException(exception,
							"Could not serialize object of class %s using mapper of class %s", value.getClass().getName(),
							field.mapper().getClass().getName());
				}
		}

		private Object serializeEnum(Object value, EnumType enumType) throws JSONProcessorException {
			Enum<?> enumeration = (Enum<?>) value;
			switch (enumType) {
				case ORDINAL:
					return enumeration.ordinal();
				case STRING:
					return enumeration.toString();
				default:
					throw new JSONProcessorException("Unreachable: enum type %s is not supported", enumType);
			}
		}

		private JSONArray serializeCollection(Collection<?> collection, FieldData<?, ?> field) throws JSONProcessorException {
			JSONArray json = new JSONArray();
			for (Object element : collection)
				if (Objects.nonNull(element))
					json.put(serializeObject(element, field));
			return json;
		}

		private JSONObject serializeMap(Map<?, ?> map, FieldData<?, ?> field) throws JSONProcessorException {
			JSONObject json = new JSONObject();
			for (Object key : map.keySet())
				if (Objects.isNull(key))
					throw new JSONProcessorException("Map key cannot be null");
				else
					json.put(//
							key.toString(), //
							serializeObject(map.get(key), field));
			return json;
		}

	}

	private static class Deserializer<O> {

		private final JSONObject deserializedJsonObject;
		private final Class<O> deserializedType;
		private final List<FieldData<?, ?>> processableFields;
		private final O deserializedObject;

		private Deserializer(Class<O> type, JSONObject json) throws JSONProcessorException {
			deserializedJsonObject = Objects.requireNonNull(json);
			deserializedType = Objects.requireNonNull(type);
			processableFields = getProcessableFields(deserializedType);
			if (processableFields.isEmpty())
				throw new JSONProcessorException("Class %s has no mappable fields", deserializedType.getName());
			try {
				deserializedObject = deserializedType.newInstance();
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not instantiate object of class %s",
						deserializedType.getName());
			}
		}

		private O deserialize() throws JSONProcessorException {
			try {
				for (FieldData<?, ?> field : processableFields)
					if (deserializedJsonObject.has(field.name()))
						field.field().set(deserializedObject, deserializeField(field, deserializedJsonObject, field.name()));
					else if (!field.nullable())
						throw new JSONProcessorException("Field %s cannot be null", field.name());
				return deserializedObject;
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not deserialize object of class %s",
						deserializedType.getName());
			}
		}

		private Object deserializeField(FieldData<?, ?> field, JSONObject json, String name) throws JSONProcessorException {
			FieldType fieldType = getFieldType(field.type());
			switch (fieldType) {
				case COLLECTION:
					return deserializeCollection(field, json.getJSONArray(name));
				case MAP:
					return deserializeMap(field, json.getJSONObject(name));
				case OBJECT:
					return deserializeObject(field, field.type(), json, name);
				default:
					throw new JSONProcessorException("Unreachable: field type %s is not supported", fieldType);
			}
		}

		@SuppressWarnings("unchecked")
		private Object deserializeObject(FieldData<?, ?> field, Class<?> type, JSONObject json, String name)
				throws JSONProcessorException {
			if (String.class.equals(type))
				return json.getString(name);
			else if (Boolean.class.equals(type))
				return json.getBoolean(name);
			else if (Integer.class.equals(type))
				return json.getInt(name);
			else if (Long.class.equals(type))
				return json.getLong(name);
			else if (Float.class.equals(type))
				return json.getFloat(name);
			else if (Double.class.equals(type))
				return json.getDouble(name);
			else if (BigDecimal.class.equals(type))
				return json.getBigDecimal(name);
			else if (BigInteger.class.equals(type))
				return json.getBigInteger(name);
			else if (Number.class.equals(type))
				return json.getNumber(name);
			else if (JSONObject.class.equals(type))
				return json.getJSONObject(name);
			else if (JSONArray.class.equals(type))
				return json.getJSONArray(name);
			else if (type.isEnum())
				return deserializeEnum(type.asSubclass(Enum.class), json.get(name), field.enumType());
			else
				return deserializeObject(type, json.get(name), field.mapper());
		}

		@SuppressWarnings("unchecked")
		private Object deserializeObject(FieldData<?, ?> field, Class<?> type, JSONArray json, int index)
				throws JSONProcessorException {
			if (String.class.equals(type))
				return json.getString(index);
			else if (Boolean.class.equals(type))
				return json.getBoolean(index);
			else if (Integer.class.equals(type))
				return json.getInt(index);
			else if (Long.class.equals(type))
				return json.getLong(index);
			else if (Float.class.equals(type))
				return json.getFloat(index);
			else if (Double.class.equals(type))
				return json.getDouble(index);
			else if (BigDecimal.class.equals(type))
				return json.getBigDecimal(index);
			else if (BigInteger.class.equals(type))
				return json.getBigInteger(index);
			else if (Number.class.equals(type))
				return json.getNumber(index);
			else if (JSONObject.class.equals(type))
				return json.getJSONObject(index);
			else if (JSONArray.class.equals(type))
				return json.getJSONArray(index);
			else if (type.isEnum())
				return deserializeEnum(type.asSubclass(Enum.class), json.get(index), field.enumType());
			else
				return deserializeObject(type, json.get(index), field.mapper());
		}

		@SuppressWarnings("unchecked")
		private <T, R> Object deserializeObject(Class<?> type, Object value, JSONValueMapper<T, R> mapper)
				throws JSONProcessorException {
			if (value instanceof JSONObject)
				return new Deserializer<>(type, (JSONObject) value).deserialize();
			else
				try {
					return mapper.fromJSONObject((R) value);
				} catch (Exception exception) {
					throw new JSONProcessorException(exception,
							"Could not deserialize object of class %s using mapper of class %s", type.getName(),
							mapper.getClass().getName());
				}
		}

		private <E extends Enum<E>> E deserializeEnum(Class<E> type, Object value, EnumType enumType)
				throws JSONProcessorException {
			switch (enumType) {
				case ORDINAL:
					return type.getEnumConstants()[Integer.parseInt(value.toString())];
				case STRING:
					return Enum.valueOf(type, value.toString());
				default:
					throw new JSONProcessorException("Unreachable: enum type %s is not supported", enumType);
			}
		}

		@SuppressWarnings("unchecked")
		private Collection<Object> deserializeCollection(FieldData<?, ?> field, JSONArray json) throws JSONProcessorException {
			Class<?> type = field.parameterizedTypes()[0];
			try {
				Collection<Object> collection = isClassInstantiable(field.type())//
						? (Collection<Object>) field.type().newInstance()//
						: field.annotation.collectionImpl().newInstance();
				for (int i = 0; i < json.length(); i++)
					collection.add(deserializeObject(field, type, json, i));
				return collection;
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not instantiate collection of class %s",
						field.annotation.collectionImpl().getName());
			}
		}

		@SuppressWarnings("unchecked")
		private Map<?, ?> deserializeMap(FieldData<?, ?> field, JSONObject json) throws JSONProcessorException {
			Class<?>[] types = field.parameterizedTypes();
			try {
				Map<Object, Object> map = isClassInstantiable(field.type())//
						? (Map<Object, Object>) field.type().newInstance()//
						: field.annotation.mapImpl().newInstance();
				for (String name : json.keySet())
					map.put(name, deserializeObject(field, types[1], json, name));
				return map;
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not instantiate map of class %s",
						field.annotation.collectionImpl().getName());
			}
		}

		private boolean isClassInstantiable(Class<?> type) {
			return !type.isInterface()//
					&& !Modifier.isAbstract(type.getModifiers())//
					&& !(type.isMemberClass() && !Modifier.isStatic(type.getModifiers()));
		}

	}

	private static class FieldData<T, R> {

		private final Field field;
		private final JSONField annotation;
		private final JSONValueMapper<T, R> mapper;

		@SuppressWarnings("unchecked")
		public FieldData(Field field, JSONField annotation) throws JSONProcessorException {
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

		public Class<?>[] parameterizedTypes() {
			ParameterizedType type = (ParameterizedType) field.getGenericType();
			Class<?>[] types = new Class<?>[type.getActualTypeArguments().length];
			for (int i = 0; i < types.length; i++)
				types[i] = (Class<?>) type.getActualTypeArguments()[i];
			return types;
		}

		public Field field() {
			return field;
		}

		public Class<?> type() {
			return field.getType();
		}

		public String name() {
			return field.getName();
		}

		public boolean nullable() {
			return annotation.nullable();
		}

		public EnumType enumType() {
			return annotation.enumType();
		}

		public JSONValueMapper<T, R> mapper() {
			return mapper;
		}

	}

	private static enum FieldType {

		OBJECT, COLLECTION, MAP;

	}

}
