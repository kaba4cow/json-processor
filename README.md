# JSON Processor Library

The **JSON Processor** library provides a simple and flexible way to convert Java objects to and from JSON representations. It includes support for custom mappings, making it easy to handle different data types and complex structures.

## Features

- Annotate fields with `@JSONField` for custom JSON processing.
- Support for nullable fields and custom value mappers.
- Built-in mappers for common Java types, including:
  - `UUID`
  - `LocalDate`
  - `LocalTime`
  - `LocalDateTime`
  - `ZonedDateTime`
  - `OffsetDateTime`
  - `Instant`
  - `Duration`

## Usage

### Annotating Fields

Use the `@JSONField` annotation to specify how fields should be processed during JSON serialization and deserialization.

**Note**: annotated fields must be non-static, non-final and not of a primitive type.

```java
public class User {

    @JSONField(nullable = false)
    private String name;

    @JSONField(nullable = true)
    private String email;

    @JSONField(enumType = JSONField.EnumType.ORDINAL)
    private Role role;
    
    public User() {}

    public static enum Role {
        GUEST, USER, ADMIN;
    }
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

### Example: serialization and deserialization

```java
User user = new User();
user.setName("User");
user.setEmail("user@example.com");
user.setRegistrationDate(LocalDate.of(2021, 7, 12));
user.setRole(User.Role.USER);

String json = JSONProcessor.serialize(user);
System.out.println("Serialized JSON: " + json);

User deserializedUser = JSONProcessor.deserialize(User.class, json);
```