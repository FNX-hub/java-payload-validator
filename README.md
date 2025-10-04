# java-payload-validator

java-payload-validator is a lightweight Java library to validate payloads (e.g. JSON) using annotations and dynamic
proxies. It provides:

- An annotation to declare a validation schema on methods.
- Payload validator implementations (e.g. JSON).
- An interceptor/proxy that validates payloads before invoking the real method.
- Support for JDK dynamic proxies and ByteBuddy subclass proxies (no cglib).

## Current validation implementations

- JSON:
    - Strict validation using Jackson (ensures strict parsing and structural checks).
    - Schema validation using Everit (org.everit.json.schema) following Everit's conventions.

## Solution (steps):

1. Use the provided annotation to mark methods that require payload validation.
2. Use the `ValidationProxyFactory` to create a proxy around your service implementation.
3. Provide or register a `PayloadValidator` implementation via a `ValidatorResolver`.

## Key features

- Declarative validation via annotation.
- Extensible: plug your own `ValidatorResolver` and `PayloadValidator`.
- Simple integration with existing code through proxy factory.
- Unit tested with JUnit 5 and Mockito (including static mocks via mockito-inline).

## Requirements

- Java 21+
- Gradle (recommended wrapper present in the repo)

## Build & Test (Gradle)

To build the project:

```
./gradlew clean build
```

To run tests:

```
./gradlew test
```

## Quick usage

1. Annotate methods with `@ValidatePayload`:

```java
public interface MyService {
    @ValidatePayload(schema = "path/to/schema/file")
    void process(MyObject payload);
}
```

2. Create a proxy that applies validation:

```java
Object payload = ...; // your payload object
MyService target = new MyServiceImpl();
MyService proxy = ValidationProxyFactory.createProxy(MyService.class, target);
proxy.

process(payload); // validation runs automatically
```

3. Implement a custom `PayloadValidator`:

```java
public class MyCustomValidator implements PayloadValidator {
    @Override
    public ValidationResult validate(String schema, Object payload) {
        // Implement validation logic (e.g. JSON Schema validation)
    }
}
```

Register/resolve it through your `ValidatorResolver` so the interceptor can find it.

## API summary

- `@ValidatePayload`: annotate methods with a schema (or alias value) to enable validation.
- `PayloadValidator`: interface exposing validate(schema, payload) -> ValidationResult.
- `ValidatorResolver`: resolves a PayloadValidator instance for the current context.
- `ValidationProxyFactory`: creates proxies (JDK dynamic proxies / ByteBuddy subclass proxies) that apply payload
  validation.

## Limitations

- The annotation-based validation relies on proxying. Therefore:
    - Methods that are not overridable (private, static, or final methods) cannot be intercepted by the proxy and will
      not have validation applied.
    - Final classes cannot be proxied by subclassing; the proxy factory cannot apply validation to instances of final
      classes via subclass proxies.
    - For reliable interception, prefer using interfaces (JDK dynamic proxies) or non-final classes with non-final
      methods if subclass proxies are supported in your build.

## Testing guidelines

- Cover methods not annotated (no validation).
- Cover annotated methods that return valid/invalid results.
- Cover interceptor behavior with JDK proxies.
- Test error cases (e.g. `InvalidPayloadException`).
- Use Mockito (with mockito-inline) for static mocking of resolvers if needed.

## Notes

- This project uses JDK dynamic proxies or ByteBuddy subclassing proxies; it does not use cglib.
- Adjust dependency versions in build.gradle to match your organization policies.

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Add tests for your changes.
4. Open a pull request with a clear description of changes.

### Commit messages

Please follow the Conventional Commits specification for commit messages to keep history consistent and enable automated release tooling.

Basic format:
```
<type>(<scope>): <short description>
```

Common types:
- feat: a new feature
- fix: a bug fix
- docs: documentation only changes
- style: formatting, missing semi-colons, etc; no code change
- refactor: code change that neither fixes a bug nor adds a feature
- perf: a code change that improves performance
- test: adding or updating tests
- chore: build process or auxiliary tool changes

Examples:
- feat(api): add schema-based validation for incoming requests
- fix(interceptor): handle null payload gracefully
- docs(readme): update usage examples

See https://www.conventionalcommits.org/ for the full specification.

## License

Apache License 2.0
