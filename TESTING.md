# Testing Guide for Code Awareness PyCharm Plugin

## Overview

This document describes the testing strategy for the Code Awareness PyCharm plugin, including unit tests, integration tests, and how to run them.

## Test Structure

### Unit Tests

Unit tests focus on testing individual components in isolation without requiring the full IntelliJ Platform.

**Location:** `src/test/java/com/codeawareness/pycharm/`

**Test Files:**
- `highlighting/ColorSchemeProviderTest.java` - Tests color scheme logic
- `highlighting/HighlightManagerTest.java` - Tests highlight manager state
- `settings/CodeAwarenessSettingsTest.java` - Tests settings persistence and toggle
- `ui/NotificationHelperTest.java` - Tests notification helper methods
- `communication/MessageProtocolTest.java` - Tests message serialization
- `communication/MessageBuilderTest.java` - Tests message building
- `communication/MessageParserTest.java` - Tests message parsing
- `utils/GuidGeneratorTest.java` - Tests GUID generation
- `utils/PathUtilsTest.java` - Tests path utilities
- `events/ResponseHandlerRegistryTest.java` - Tests response handler registry

### Integration Tests (Future)

Integration tests would use IntelliJ Platform test fixtures to test components in a real IDE environment.

**Recommended Approach:**
```java
public class HighlightManagerIntegrationTest extends LightPlatformTestCase {

    public void testAddHighlight() {
        // Create a test file
        VirtualFile file = myFixture.addFileToProject("test.py", "print('hello')");

        // Open in editor
        myFixture.openFileInEditor(file);
        Editor editor = myFixture.getEditor();

        // Get highlight manager
        HighlightManager manager = new HighlightManager(getProject());

        // Add highlight
        manager.addHighlight(file.getPath(), 0);

        // Verify highlight was added
        assertEquals(1, manager.getHighlightCount(file.getPath()));
    }
}
```

## Running Tests

### Via Gradle Command Line

```bash
# Run all tests
gradle test

# Run specific test class
gradle test --tests "ColorSchemeProviderTest"

# Run tests with verbose output
gradle test --info

# Run tests and generate coverage report
gradle test jacocoTestReport
```

### Via IntelliJ IDEA

1. Right-click on test file or test class
2. Select "Run 'ClassName'" or "Run 'testMethodName()'"
3. View results in Run tool window

### Via Command Line (direct)

```bash
# Run all tests
./gradlew test

# Run specific test package
./gradlew test --tests "com.codeawareness.pycharm.highlighting.*"
```

## Test Dependencies

The project uses the following testing frameworks:

- **JUnit 5 (Jupiter)** - Main testing framework
- **Mockito 5** - Mocking framework for unit tests
- **IntelliJ Platform Test Framework** - For integration tests (future)

Dependencies are configured in `build.gradle.kts`:
```kotlin
testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
testImplementation("org.mockito:mockito-core:5.5.0")
```

## Writing Tests

### Unit Test Example

```java
@Test
void testToggleHighlights() {
    CodeAwarenessSettings settings = new CodeAwarenessSettings();

    // Initially enabled
    assertTrue(settings.isHighlightsEnabled());

    // Toggle to disabled
    boolean newState = settings.toggleHighlights();
    assertFalse(newState);
    assertFalse(settings.isHighlightsEnabled());

    // Toggle to enabled
    newState = settings.toggleHighlights();
    assertTrue(newState);
    assertTrue(settings.isHighlightsEnabled());
}
```

### Mock Example

```java
@Test
void testWithMockedProject() {
    Project mockProject = Mockito.mock(Project.class);
    Mockito.when(mockProject.getName()).thenReturn("TestProject");

    HighlightManager manager = new HighlightManager(mockProject);
    assertNotNull(manager);
}
```

## Test Coverage

Current test coverage focuses on:

1. **Communication Layer** - Message protocol, parsing, building
2. **Utility Classes** - GUID generation, path utilities
3. **Event System** - Response handler registry
4. **UI Components** - Settings, color schemes, notification helpers
5. **Highlighting** - Highlight manager state management

## Known Limitations

### Components Requiring Integration Tests

The following components require IntelliJ Platform test fixtures for full testing:

1. **HighlightManager.addHighlight()** - Requires real Editor and MarkupModel
2. **NotificationHelper** - Requires NotificationGroupManager and Project
3. **StatusBarWidget** - Requires StatusBar and UI threading
4. **File Listeners** - Requires VFS and file events
5. **Project Services** - Requires Project initialization

These components have basic unit tests that verify logic without full platform mocking.

## Future Improvements

### Integration Test Setup

To add full integration tests:

1. **Configure IntelliJ Platform Test Framework:**
```kotlin
// Add to build.gradle.kts
dependencies {
    testImplementation("com.jetbrains.intellij.platform:test-framework:2023.3")
}
```

2. **Create Integration Test Base:**
```java
public abstract class CodeAwarenessIntegrationTest extends LightPlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }
}
```

3. **Add Test Fixtures:**
```
src/test/testData/
  ├── test.py
  ├── test.java
  └── test_project/
```

### UI Testing

For UI component testing:

1. Use `com.intellij.testFramework.fixtures.CodeInsightTestFixture`
2. Mock StatusBar interactions
3. Verify widget state changes
4. Test notification display

## Continuous Integration

Tests are automatically run in CI/CD pipeline (if configured).

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Upload test results
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: build/test-results/
```

## Test Maintenance

- Keep tests independent and idempotent
- Use descriptive test names (testFeatureUnderTest_Condition_ExpectedResult)
- Add tests for bug fixes to prevent regressions
- Update tests when refactoring code
- Aim for >80% code coverage for testable components

## Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [IntelliJ Platform Plugin Testing](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html)
- [IntelliJ Platform SDK DevGuide](https://plugins.jetbrains.com/docs/intellij/welcome.html)
