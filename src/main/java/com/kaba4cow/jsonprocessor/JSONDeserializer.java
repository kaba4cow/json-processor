package com.kaba4cow.jsonprocessor;

import java.lang.reflect.Modifier;
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

import com.kaba4cow.jsonprocessor.ProcessingHelper.FieldData;
import com.kaba4cow.jsonprocessor.ProcessingHelper.FieldType;

/**
 * Utility class for deserializing JSON data to Java objects. This class provides methods to convert JSON objects to their Java
 * object representation.
 */
public class JSONDeserializer {

	private JSONDeserializer() {}

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
	 * 
	 * @see #processAll(Class, JSONArray)
	 * @see #processAll(Class, JSONObject)
	 */
	public static <T> T process(Class<T> type, JSONObject json) throws JSONProcessorException {
		synchronized (json) {
			return new Converter<>(type, json).deserialize();
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
	 * 
	 * @see #process(Class, JSONObject)
	 * @see #processAll(Class, JSONObject)
	 */
	public static <T> List<T> processAll(Class<T> type, JSONArray json) throws JSONProcessorException {
		synchronized (json) {
			List<T> list = new ArrayList<>();
			for (int i = 0; i < json.length(); i++)
				list.add(process(type, json.getJSONObject(i)));
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
	 * 
	 * @see #process(Class, JSONObject)
	 * @see #processAll(Class, JSONArray)
	 */
	public static <T> Map<String, T> processAll(Class<T> type, JSONObject json) throws JSONProcessorException {
		synchronized (json) {
			Map<String, T> map = new HashMap<>();
			for (String key : json.keySet())
				map.put(key, process(type, json.getJSONObject(key)));
			return map;
		}
	}

	private static class Converter<O> {

		private final JSONObject deserializedJsonObject;
		private final Class<O> deserializedType;
		private final List<FieldData<?, ?>> processableFields;
		private final O deserializedObject;

		private Converter(Class<O> type, JSONObject json) throws JSONProcessorException {
			deserializedJsonObject = Objects.requireNonNull(json);
			deserializedType = Objects.requireNonNull(type);
			processableFields = ProcessingHelper.getProcessableFields(deserializedType);
			if (processableFields.isEmpty())
				throw new JSONProcessorException("Class %s has no processable fields", deserializedType.getName());
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
			FieldType fieldType = ProcessingHelper.getFieldType(field.type());
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
				return deserializeEnum(type.asSubclass(Enum.class), json.get(name), field.enumFormat());
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
				return deserializeEnum(type.asSubclass(Enum.class), json.get(index), field.enumFormat());
			else
				return deserializeObject(type, json.get(index), field.mapper());
		}

		@SuppressWarnings("unchecked")
		private <T, R> Object deserializeObject(Class<?> type, Object value, JSONValueMapper<T, R> mapper)
				throws JSONProcessorException {
			if (value instanceof JSONObject)
				return new Converter<>(type, (JSONObject) value).deserialize();
			else
				try {
					return mapper.fromJSONObject((R) value);
				} catch (Exception exception) {
					throw new JSONProcessorException(exception,
							"Could not deserialize object of class %s using mapper of class %s", type.getName(),
							mapper.getClass().getName());
				}
		}

		private <E extends Enum<E>> E deserializeEnum(Class<E> type, Object value, EnumFormat enumFormat)
				throws JSONProcessorException {
			switch (enumFormat) {
				case ORDINAL:
					return type.getEnumConstants()[Integer.parseInt(value.toString())];
				case STRING:
					return Enum.valueOf(type, value.toString());
				default:
					throw new JSONProcessorException("Unreachable: enum type %s is not supported", enumFormat);
			}
		}

		@SuppressWarnings("unchecked")
		private Collection<Object> deserializeCollection(FieldData<?, ?> field, JSONArray json) throws JSONProcessorException {
			Class<?> type = field.parameterizedTypes()[0];
			try {
				Collection<Object> collection = isClassInstantiable(field.type())//
						? (Collection<Object>) field.type().newInstance()//
						: field.collectionImpl().newInstance();
				for (int i = 0; i < json.length(); i++)
					collection.add(deserializeObject(field, type, json, i));
				return collection;
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not instantiate collection of class %s",
						field.collectionImpl().getName());
			}
		}

		@SuppressWarnings("unchecked")
		private Map<?, ?> deserializeMap(FieldData<?, ?> field, JSONObject json) throws JSONProcessorException {
			Class<?>[] types = field.parameterizedTypes();
			try {
				Map<Object, Object> map = isClassInstantiable(field.type())//
						? (Map<Object, Object>) field.type().newInstance()//
						: field.mapImpl().newInstance();
				for (String name : json.keySet())
					map.put(name, deserializeObject(field, types[1], json, name));
				return map;
			} catch (Exception exception) {
				throw new JSONProcessorException(exception, "Could not instantiate map of class %s",
						field.collectionImpl().getName());
			}
		}

		private boolean isClassInstantiable(Class<?> type) {
			return !type.isInterface()//
					&& !Modifier.isAbstract(type.getModifiers())//
					&& !(type.isMemberClass() && !Modifier.isStatic(type.getModifiers()));
		}

	}

}