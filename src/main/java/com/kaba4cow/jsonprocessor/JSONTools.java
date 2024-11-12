package com.kaba4cow.jsonprocessor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class providing methods to convert Java collections to JSON data structured, and vice versa. It also includes methods
 * for iterating over JSONObjects and JSONArrays.
 */
public class JSONTools {

	private JSONTools() {}

	/**
	 * Converts an array to a JSONArray using the specified element mapper function.
	 *
	 * @param <E>           the type of the array elements
	 * @param array         the array to convert
	 * @param elementMapper a function to map each element to a JSON-compatible value
	 * 
	 * @return a JSONArray containing the mapped elements
	 */
	public static <E> JSONArray toJSONArray(E[] array, Function<E, ?> elementMapper) {
		JSONArray json = new JSONArray();
		for (E element : array)
			json.put(elementMapper.apply(element));
		return json;
	}

	/**
	 * Converts an array to a JSONArray.
	 *
	 * @param <E>   the type of the array elements
	 * @param array the array to convert
	 * 
	 * @return a JSONArray containing the array elements
	 */
	public static <E> JSONArray toJSONArray(E[] array) {
		JSONArray json = new JSONArray();
		for (E element : array)
			json.put(element);
		return json;
	}

	/**
	 * Converts a collection to a JSONArray using the specified element mapper function.
	 *
	 * @param <E>           the type of the collection elements
	 * @param collection    the collection to convert
	 * @param elementMapper a function to map each element to a JSON-compatible value
	 * 
	 * @return a JSONArray containing the mapped elements
	 */
	public static <E> JSONArray toJSONArray(Collection<E> collection, Function<E, ?> elementMapper) {
		JSONArray json = new JSONArray();
		for (E element : collection)
			json.put(elementMapper.apply(element));
		return json;
	}

	/**
	 * Converts a collection to a JSONArray.
	 *
	 * @param <E>        the type of the collection elements
	 * @param collection the collection to convert
	 * 
	 * @return a JSONArray containing the collection elements
	 */
	public static <E> JSONArray toJSONArray(Collection<E> collection) {
		JSONArray json = new JSONArray();
		for (E element : collection)
			json.put(element);
		return json;
	}

	/**
	 * Converts a map to a JSONObject using the specified key mapper function.
	 *
	 * @param <K>       the type of the map keys
	 * @param <V>       the type of the map values
	 * @param map       the map to convert
	 * @param keyMapper a function to map each key to a string
	 * 
	 * @return a JSONObject containing the mapped keys and values
	 */
	public static <K, V> JSONObject toJSONObject(Map<K, V> map, Function<K, String> keyMapper) {
		JSONObject json = new JSONObject();
		for (K key : map.keySet())
			json.put(keyMapper.apply(key), map.get(key));
		return json;
	}

	/**
	 * Converts a map with string keys to a JSONObject.
	 *
	 * @param <V> the type of the map values
	 * @param map the map to convert
	 * 
	 * @return a JSONObject containing the map's keys and values
	 */
	public static <V> JSONObject toJSONObject(Map<String, V> map) {
		JSONObject json = new JSONObject();
		for (String key : map.keySet())
			json.put(key, map.get(key));
		return json;
	}

	/**
	 * Converts a JSONObject to a map using the specified key mapper and value extractor functions.
	 *
	 * @param <K>          the type of the map keys
	 * @param <V>          the type of the map values
	 * @param json         the JSONObject to convert
	 * @param keyMapper    a function to map each JSON key to a map key
	 * @param jsonFunction a function to extract values from the JSONObject
	 * 
	 * @return a map containing the converted keys and values
	 */
	public static <K, V> Map<K, V> toMap(JSONObject json, Function<String, K> keyMapper,
			BiFunction<JSONObject, String, V> jsonFunction) {
		Map<K, V> map = new LinkedHashMap<>();
		for (String key : json.keySet())
			map.put(keyMapper.apply(key), jsonFunction.apply(json, key));
		return map;
	}

	/**
	 * Converts a JSONObject to a map with string keys.
	 *
	 * @param <V>          the type of the map values
	 * @param json         the JSONObject to convert
	 * @param jsonFunction a function to extract values from the JSONObject
	 * 
	 * @return a map containing the JSON object's keys and values
	 */
	public static <V> Map<String, V> toMap(JSONObject json, BiFunction<JSONObject, String, V> jsonFunction) {
		Map<String, V> map = new LinkedHashMap<>();
		for (String key : json.keySet())
			map.put(key, jsonFunction.apply(json, key));
		return map;
	}

	/**
	 * Converts a JSONArray to a list using the specified value extractor function.
	 *
	 * @param <E>          the type of the list elements
	 * @param json         the JSONArray to convert
	 * @param jsonFunction a function to extract values from the JSONArray
	 * 
	 * @return a list containing the extracted values
	 */
	public static <E> List<E> toList(JSONArray json, BiFunction<JSONArray, Integer, E> jsonFunction) {
		List<E> list = new ArrayList<>();
		for (int i = 0; i < json.length(); i++)
			list.add(jsonFunction.apply(json, i));
		return list;
	}

	/**
	 * Converts a JSONArray to a list of strings.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of strings
	 */
	public static List<String> toStringList(JSONArray json) {
		return toList(json, JSONArray::getString);
	}

	/**
	 * Converts a JSONArray to a list of booleans.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of booleans
	 */
	public static List<Boolean> toBooleanList(JSONArray json) {
		return toList(json, JSONArray::getBoolean);
	}

	/**
	 * Converts a JSONArray to a list of ints.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of ints
	 */
	public static List<Integer> toIntList(JSONArray json) {
		return toList(json, JSONArray::getInt);
	}

	/**
	 * Converts a JSONArray to a list of longs.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of longs
	 */
	public static List<Long> toLongList(JSONArray json) {
		return toList(json, JSONArray::getLong);
	}

	/**
	 * Converts a JSONArray to a list of floats.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of floats
	 */
	public static List<Float> toFloatList(JSONArray json) {
		return toList(json, JSONArray::getFloat);
	}

	/**
	 * Converts a JSONArray to a list of doubles.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of doubles
	 */
	public static List<Double> toDoubleList(JSONArray json) {
		return toList(json, JSONArray::getDouble);
	}

	/**
	 * Converts a JSONArray to a list of BigDecimals.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of BigDecimals
	 */
	public static List<BigDecimal> toBigDecimalList(JSONArray json) {
		return toList(json, JSONArray::getBigDecimal);
	}

	/**
	 * Converts a JSONArray to a list of BigIntegers.
	 *
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of BigIntegers
	 */
	public static List<BigInteger> toBigIntegerList(JSONArray json) {
		return toList(json, JSONArray::getBigInteger);
	}

	/**
	 * Converts a JSONArray to a list of enums of a specified type.
	 *
	 * @param type the enum type
	 * @param json the JSONArray to convert
	 * 
	 * @return a list of enums
	 */
	public static <E extends Enum<E>> List<E> toEnumList(Class<E> type, JSONArray json) {
		return toList(json, (array, i) -> array.getEnum(type, i));
	}

	/**
	 * Iterates over a JSONArray, applying a value extractor function and executing the specified action for each value.
	 *
	 * @param <E>          the type of the extracted values
	 * @param json         the JSONArray to iterate over
	 * @param jsonFunction a function to extract values from the JSONArray
	 * @param action       a consumer to process each extracted value
	 */
	public static <E> void forEach(JSONArray json, BiFunction<JSONArray, Integer, E> jsonFunction, Consumer<E> action) {
		for (int i = 0; i < json.length(); i++)
			action.accept(jsonFunction.apply(json, i));
	}

	/**
	 * Iterates over a JSONObject, applying a value extractor function and executing the specified action for each value.
	 *
	 * @param <E>          the type of the extracted values
	 * @param json         the JSONObject to iterate over
	 * @param jsonFunction a function to extract values from the JSONObject
	 * @param action       a consumer to process each extracted value
	 */
	public static <E> void forEach(JSONObject json, BiFunction<JSONObject, String, E> jsonFunction, Consumer<E> action) {
		for (String key : json.keySet())
			action.accept(jsonFunction.apply(json, key));
	}

}
