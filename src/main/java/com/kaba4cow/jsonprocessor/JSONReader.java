package com.kaba4cow.jsonprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for reading {@link JSONObject} and {@link JSONArray} objects from various data sources. Provides static methods
 * to load JSON data.
 * 
 * @see URL
 * @see Path
 * @see File
 * @see InputStream
 * @see Reader
 */
public class JSONReader {

	private JSONReader() {}

	/**
	 * Reads a {@link JSONObject} from the specified {@link URL}.
	 *
	 * @param url the URL to read JSON data from
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONObject readJSONObject(URL url) throws JSONException, IOException {
		return new JSONObject(readSourceFromURL(url));
	}

	/**
	 * Reads a {@link JSONObject} from the specified {@link Path}.
	 *
	 * @param path the file path to read JSON data from
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONObject readJSONObject(Path path) throws JSONException, IOException {
		return new JSONObject(readSourceFromPath(path));
	}

	/**
	 * Reads a {@link JSONObject} from the specified byte array.
	 *
	 * @param bytes the byte array containing JSON data
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if an error occurs while processing the data
	 */
	public static JSONObject readJSONObject(byte[] bytes) throws JSONException, IOException {
		return new JSONObject(readSourceFromBytes(bytes));
	}

	/**
	 * Reads a {@link JSONObject} from the specified {@link File}.
	 *
	 * @param file the file to read JSON data from
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONObject readJSONObject(File file) throws JSONException, IOException {
		return new JSONObject(readSourceFromFile(file));
	}

	/**
	 * Reads a {@link JSONObject} from the specified resource.
	 *
	 * @param resourceName the name of the resource to load JSON data from
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if the resource is not found or an I/O error occurs
	 */
	public static JSONObject readJSONObject(String resourceName) throws JSONException, IOException {
		return new JSONObject(readSourceFromResources(resourceName));
	}

	/**
	 * Reads a {@link JSONObject} from the specified {@link InputStream}.
	 *
	 * @param input the input stream to read JSON data from
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONObject readJSONObject(InputStream input) throws JSONException, IOException {
		return new JSONObject(readSourceFromInputStream(input));
	}

	/**
	 * Reads a {@link JSONObject} from the specified {@link Reader}.
	 *
	 * @param reader the reader to read JSON data from
	 * 
	 * @return a {@link JSONObject} containing the data
	 * 
	 * @throws JSONException if there is a syntax error in the source string or a duplicated key
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONObject readJSONObject(Reader reader) throws JSONException, IOException {
		return new JSONObject(readSourceFromReader(reader));
	}

	/**
	 * Reads a {@link JSONArray} from the specified {@link URL}.
	 *
	 * @param url the URL to read JSON data from
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONArray readJSONArray(URL url) throws JSONException, IOException {
		return new JSONArray(readSourceFromURL(url));
	}

	/**
	 * Reads a {@link JSONArray} from the specified {@link Path}.
	 *
	 * @param path the file path to read JSON data from
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONArray readJSONArray(Path path) throws JSONException, IOException {
		return new JSONArray(readSourceFromPath(path));
	}

	/**
	 * Reads a {@link JSONArray} from the specified byte array.
	 *
	 * @param bytes the byte array containing JSON data
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if an error occurs while processing the data
	 */
	public static JSONArray readJSONArray(byte[] bytes) throws JSONException, IOException {
		return new JSONArray(readSourceFromBytes(bytes));
	}

	/**
	 * Reads a {@link JSONArray} from the specified {@link File}.
	 *
	 * @param file the file to read JSON data from
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONArray readJSONArray(File file) throws JSONException, IOException {
		return new JSONArray(readSourceFromFile(file));
	}

	/**
	 * Reads a {@link JSONArray} from the specified resource.
	 *
	 * @param resourceName the name of the resource to load JSON data from
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if the resource is not found or an I/O error occurs
	 */
	public static JSONArray readJSONArray(String resourceName) throws JSONException, IOException {
		return new JSONArray(readSourceFromResources(resourceName));
	}

	/**
	 * Reads a {@link JSONArray} from the specified {@link InputStream}.
	 *
	 * @param input the input stream to read JSON data from
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONArray readJSONArray(InputStream input) throws JSONException, IOException {
		return new JSONArray(readSourceFromInputStream(input));
	}

	/**
	 * Reads a {@link JSONArray} from the specified {@link Reader}.
	 *
	 * @param reader the reader to read JSON data from
	 * 
	 * @return a {@link JSONArray} containing the data
	 * 
	 * @throws JSONException if there is a syntax error
	 * @throws IOException   if an I/O error occurs
	 */
	public static JSONArray readJSONArray(Reader reader) throws JSONException, IOException {
		return new JSONArray(readSourceFromReader(reader));
	}

	private static String readSourceFromURL(URL url) throws IOException {
		return readSourceFromInputStream(url.openStream());
	}

	private static String readSourceFromPath(Path path) throws IOException {
		return readSourceFromBytes(Files.readAllBytes(path));
	}

	private static String readSourceFromBytes(byte[] bytes) throws IOException {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private static String readSourceFromFile(File file) throws IOException {
		return readSourceFromInputStream(new FileInputStream(file));
	}

	private static String readSourceFromResources(String resourceName) throws IOException {
		return readSourceFromInputStream(JSONReader.class.getClassLoader().getResourceAsStream(resourceName));
	}

	private static String readSourceFromInputStream(InputStream input) throws IOException {
		return readSourceFromReader(new InputStreamReader(input));
	}

	private static String readSourceFromReader(Reader reader) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		StringBuilder source = new StringBuilder();
		String line = null;
		while ((line = bufferedReader.readLine()) != null)
			source.append(line);
		bufferedReader.close();
		return source.toString();
	}

}
