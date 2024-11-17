# JSON Processor Library

The **JSON Processor** library provides a simple and flexible way to convert **Java** objects to and from **JSON** representations. It includes support for custom mappings, making it easy to handle different data types and complex structures, and implementation qualifiers for seamlsess deserialization of `Collection` and `Map` fields.

## Features

- Annotate fields with `@JSONField` for custom **JSON** processing.
- Support for nullable fields and custom value mappers.
- Built-in mappers for common **Java** types, including:
  - `UUID`
  - `LocalDate`, `LocalTime`, `LocalDateTime`
  - `ZonedDateTime`, `OffsetDateTime`, `Instant`
  - `Duration`
- Built-in mappers for hexadecimal number format conversion:
  - `Hexadecimal.Integer`
  - `Hexadecimal.Long`
- `JSONTools` utility class for manual conversion between **Java** objects and **JSON** data and iterating over `JSONObject` and `JSONArray`
- `JSONReader` utility class for reading **JSON** data from various data sources

## Usage

### Annotating Fields

Use the `@JSONField` annotation to specify how fields should be processed during **JSON** serialization and deserialization.

**Note**: annotated fields must be non-static and non-final;

```java
public class User {

    @JSONField(nullable = false)
    private String name;

    @JSONField(nullable = true)
    private String email;
    
    public User() {}
}
```

### Using Value Mappers

To apply a value mapper to a specific field, use the `@JSONField` annotation's `mapper` attribute.

```java
@JSONField(mapper = JSONValueMapper.LocalDate.class)
private LocalDate registrationDate;
```

### Custom Value Mappers

To create a custom value mapper simply create a class implementing the `JSONValueMapper`.

```java
public class EncryptedStringMapper implements JSONValueMapper<String, String> {

    @Override
    public String toJSONObject(String value) throws Exception {
        return encrypt(value);
    }

    @Override
    public String fromJSONObject(String value) throws Exception {
        return decrypt(value);
    }

    private String encrypt(String value) {
        // Encryption logic...
    }

    private String decrypt(String value) {
        // Decryption logic...
    }
}
```

### Enumeration serialization

The `@JSONField` annotation includes the `enumType` attribute, which defines how fields of type `enum` are serialized and deserialized.

- Purpose: the `enumType` attribute determines whether an `enum` field should be serialized or deserialized as its name (string) or its ordinal (integer) value.
- Default value: `EnumType.STRING`
- Usage: `EnumType.STRING` option is the most human-readable option and is ideal for scenarios where JSON data is manually reviewed or edited. `EnumType.ORDINAL` may be useful when storage efficiency is a priority.

```java
@JSONField(enumType = JSONField.EnumType.ORDINAL)
private Role role;

public static enum Role {
    GUEST, USER, ADMIN;
}
```

### Implementation qualifiers

The `collectionImpl` and `mapImpl` attributes allow customization of how collection and map fields are deserialized.

`collectionImpl`

- Purpose: defines the concrete implementation for fields of type `Collection` or its subtypes.
- Default value: `ArrayList.class`
- Usage: when deserializing a field that is declared as a `Collection` (e.g. `List`, `Set`), `collectionImpl` specifies which implementation to instantiate if the field's type is an interface, abstract class, or a non-static inner class. 

  For example, if a field is declared as `List<String>`, the deserialization process will use `ArrayList` to create an instance unless another implementation is provided.
   
`mapImpl`

- Purpose: defines the concrete implementation for fields of type Map or its subtypes.
- Default value: `HashMap.class`
- Usage: when deserializing a field that is declared as a `Map` (e.g. `Map<String, String>`), `mapImpl` specifies which implementation to instantiate if the field's type is an interface, abstract class, or a non-static inner class.

  For instance, a `Map<String, String>` field will default to a `HashMap` unless another implementation is explicitly provided.

```java
@JSONField(collectionImpl = HashSet.class)
private Collection<String> cars;

@JSONField(mapImpl = TreeMap.class)
private Map<String, String> messages;
```

Usage of these attributes ensures seamless deserialization even when the field's declared type is an abstract type or interface.

### Serialization and deserialization

To serialize and deserialize objects use `JSONSerializer` and `JSONDeserializer` classes `process` methods.

### Error Handling

If an exception occurs during JSON serialization or deserialization, it will be wrapped in an `JSONProcessorException`.

## Example

```java
public class Example {

	public static class User {

		@JSONField(nullable = false)
		private String name;

		@JSONField(nullable = true)
		private String email;

		@JSONField(enumType = EnumType.STRING)
		private Role role;

		@JSONField(collectionImpl = HashSet.class)
		private Collection<String> cars;

		@JSONField(mapImpl = TreeMap.class)
		private Map<String, String> messages;

		public User() {}

		@Override
		public String toString() {
			return String.format("User [name=%s, email=%s, role=%s, cars=%s, messages=%s]", 
				name, email, role, cars, messages);
		}

	}

	public static enum Role {
		GUEST, USER, ADMIN;
	}

	public static void main(String[] args) throws JSONProcessorException {
		User user = new User();
		user.name = "Tom";
		user.email = "user@example.com";
		user.role = Role.GUEST;

		user.cars = new HashSet<>();
		user.cars.add("Bolt V8");
		user.cars.add("Falconer");
		user.cars.add("Shubert Six");

		user.messages = new TreeMap<>();
		user.messages.put("Paulie", "Hi!");
		user.messages.put("Sam", "What's up");

		JSONObject json = JSONSerializer.process(user);
		System.out.println("Serialized JSON: " + json.toString(1));

		User deserialized = JSONDeserializer.process(User.class, json);
		System.out.println("Deserialized User: " + deserialized.toString());
	}

}
```