package com.kaba4cow.jsonprocessor;

import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Interface for mapping values to and from JSON-compatible representations.
 *
 * @param <T> the type of the value being converted
 * @param <R> the type of the JSON-compatible representation
 */
public interface JSONValueMapper<T, R> {

	/**
	 * Converts a value to its JSON-compatible representation.
	 *
	 * @param value the value to convert
	 * 
	 * @return the JSON-compatible representation of the value
	 * 
	 * @throws Exception if an error occurs during conversion
	 */
	public R toJSONObject(T value) throws Exception;

	/**
	 * Converts a JSON-compatible representation back to its original value.
	 *
	 * @param value the JSON-compatible representation
	 * 
	 * @return the original value
	 * 
	 * @throws Exception if an error occurs during conversion
	 */
	public T fromJSONObject(R value) throws Exception;

	/**
	 * Default implementation of {@link JSONValueMapper} that performs no conversion and is used by default.
	 *
	 * @param <T> the type of the value being converted
	 */
	public static class Default<T> implements JSONValueMapper<T, T> {

		@Override
		public T toJSONObject(T value) {
			return value;
		}

		@Override
		public T fromJSONObject(T value) {
			return value;
		}

	}

	/**
	 * Mapper for converting {@link java.util.UUID} to and from a {@link String}.
	 */
	public static class UUID implements JSONValueMapper<java.util.UUID, String> {

		@Override
		public String toJSONObject(java.util.UUID value) throws Exception {
			return value.toString();
		}

		@Override
		public java.util.UUID fromJSONObject(String value) throws Exception {
			return java.util.UUID.fromString(value);
		}

	}

	/**
	 * Mapper for converting {@link java.time.LocalDate} to and from a {@link Number} (epoch day).
	 */
	public static class LocalDate implements JSONValueMapper<java.time.LocalDate, Number> {

		@Override
		public Long toJSONObject(java.time.LocalDate value) throws Exception {
			return value.toEpochDay();
		}

		@Override
		public java.time.LocalDate fromJSONObject(Number value) throws Exception {
			return java.time.LocalDate.ofEpochDay(value.longValue());
		}

	}

	/**
	 * Mapper for converting {@link java.time.LocalTime} to and from a {@link Number} (nanoseconds of day).
	 */
	public static class LocalTime implements JSONValueMapper<java.time.LocalTime, Number> {

		@Override
		public Long toJSONObject(java.time.LocalTime value) throws Exception {
			return value.toNanoOfDay();
		}

		@Override
		public java.time.LocalTime fromJSONObject(Number value) throws Exception {
			return java.time.LocalTime.ofNanoOfDay(value.longValue());
		}

	}

	/**
	 * Mapper for converting {@link java.time.LocalDateTime} to and from a {@link Number} (epoch milliseconds).
	 */
	public static class LocalDateTime implements JSONValueMapper<java.time.LocalDateTime, Number> {

		@Override
		public Long toJSONObject(java.time.LocalDateTime value) throws Exception {
			return value.toInstant(ZoneOffset.UTC).toEpochMilli();
		}

		@Override
		public java.time.LocalDateTime fromJSONObject(Number value) throws Exception {
			return java.time.LocalDateTime.ofEpochSecond(value.longValue() / 1000L, 0, ZoneOffset.UTC);
		}

	}

	/**
	 * Mapper for converting {@link java.time.ZonedDateTime} to and from a {@link Number} (epoch milliseconds).
	 */
	public static class ZonedDateTime implements JSONValueMapper<java.time.ZonedDateTime, Number> {

		@Override
		public Long toJSONObject(java.time.ZonedDateTime value) throws Exception {
			return value.toInstant().toEpochMilli();
		}

		@Override
		public java.time.ZonedDateTime fromJSONObject(Number value) throws Exception {
			return java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(value.longValue()), ZoneId.systemDefault());
		}

	}

	/**
	 * Mapper for converting {@link java.time.OffsetDateTime} to and from a {@link Number} (epoch milliseconds).
	 */
	public static class OffsetDateTime implements JSONValueMapper<java.time.OffsetDateTime, Number> {

		@Override
		public Long toJSONObject(java.time.OffsetDateTime value) throws Exception {
			return value.toInstant().toEpochMilli();
		}

		@Override
		public java.time.OffsetDateTime fromJSONObject(Number value) throws Exception {
			return java.time.OffsetDateTime.ofInstant(java.time.Instant.ofEpochMilli(value.longValue()), ZoneOffset.UTC);
		}

	}

	/**
	 * Mapper for converting {@link java.time.Instant} to and from a {@link Number} (epoch milliseconds).
	 */
	public static class Instant implements JSONValueMapper<java.time.Instant, Number> {

		@Override
		public Long toJSONObject(java.time.Instant value) throws Exception {
			return value.toEpochMilli();
		}

		@Override
		public java.time.Instant fromJSONObject(Number value) throws Exception {
			return java.time.Instant.ofEpochMilli(value.longValue());
		}

	}

	/**
	 * Mapper for converting {@link java.time.Duration} to and from a {@link Number} (milliseconds).
	 */
	public static class Duration implements JSONValueMapper<java.time.Duration, Number> {

		@Override
		public Long toJSONObject(java.time.Duration value) throws Exception {
			return value.toMillis();
		}

		@Override
		public java.time.Duration fromJSONObject(Number value) throws Exception {
			return java.time.Duration.ofMillis(value.longValue());
		}

	}

}
