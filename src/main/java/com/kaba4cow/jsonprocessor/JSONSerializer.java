package com.kaba4cow.jsonprocessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kaba4cow.jsonprocessor.ProcessingHelper.FieldData;
import com.kaba4cow.jsonprocessor.ProcessingHelper.FieldType;

/**
 * Utility class for serializing Java objects to JSON. This class provides methods to convert objects into their JSON
 * representation.
 */
public class JSONSerializer {

	private JSONSerializer() {}

	/**
	 * Serializes the specified object into a {@link JSONObject}.
	 *
	 * @param object the object to serialize
	 * 
	 * @return a {@link JSONObject} representing the object
	 * 
	 * @throws JSONProcessorException if serialization fails
	 * 
	 * @see #processAll(Class, JSONArray)
	 * @see #processAll(Class, JSONObject)
	 */
	public static JSONObject process(Object object) throws JSONProcessorException {
		synchronized (object) {
			return new Converter(object).serialize();
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
	 * 
	 * @see #process(Class, JSONObject)
	 * @see #processAll(Class, JSONObject)
	 */
	public static <T> JSONArray processAll(Collection<T> collection) throws JSONProcessorException {
		synchronized (collection) {
			JSONArray json = new JSONArray();
			for (T object : collection)
				json.put(process(object));
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
	 * 
	 * @see #process(Class, JSONObject)
	 * @see #processAll(Class, JSONArray)
	 */
	public static <T> JSONObject processAll(Map<String, T> map) throws JSONProcessorException {
		synchronized (map) {
			JSONObject json = new JSONObject();
			for (String key : map.keySet())
				json.put(key, process(map.get(key)));
			return json;
		}
	}

	private static class Converter {

		private final Object serializedObject;
		private final Class<?> serializedType;
		private final List<FieldData<?, ?>> processableFields;
		private final JSONObject serializedJsonObject;

		private Converter(Object object) throws JSONProcessorException {
			serializedObject = Objects.requireNonNull(object);
			serializedType = serializedObject.getClass();
			processableFields = ProcessingHelper.getProcessableFields(serializedType);
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
						FieldType fieldType = ProcessingHelper.getFieldType(value.getClass());
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
					return ProcessingHelper.hasProcessableFields(value.getClass()) //
							? new Converter(value).serialize() //
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

}
