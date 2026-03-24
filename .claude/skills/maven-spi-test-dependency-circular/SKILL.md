---
name: maven-spi-test-dependency-circular
description: |
  Fix Maven SPI (ServiceLoader) test failures caused by circular module dependencies.
  Use when: (1) ServiceLoader can't find implementation in tests, (2) Module A implements 
  SPI but depends on Module B which contains the ServiceLoader, (3) Adding test dependency 
  creates circular reference error. Solution: Create mock SPI implementation in test 
  classpath instead of depending on real implementation module.
author: Claude Code
version: 1.0.0
date: 2026-03-24
---

# Maven SPI Test Dependency Circular Resolution

## Problem

When testing ServiceLoader-based SPI (Service Provider Interface) in a multi-module Maven 
project, the ServiceLoader can't find implementations because:
- Implementation module (e.g., `aidrclaw-plugins-storage`) depends on core module 
  (e.g., `aidrclaw-core`) for entity classes and mappers
- Adding implementation as test dependency to core creates circular dependency
- ServiceLoader only finds SPI implementations on the classpath

## Context / Trigger Conditions
- Maven build error: "The projects in the reactor contain a cyclic reference"
- ServiceLoader.load(ServiceInterface.class) returns empty iterator in tests
- SPI implementation exists but is in a different module that depends on the testing module
- Unit tests fail with "expected: <true> but was: <false>" for plugin loading

## Solution

**Option 1: Mock SPI Implementation (Recommended for Unit Tests)**

1. Create a mock implementation in the test directory:
   ```
   src/test/java/com/example/core/plugin/MockPlugin.java
   ```

2. Create SPI configuration in test resources:
   ```
   src/test/resources/META-INF/services/com.example.plugin.Plugin
   ```
   
   Content:
   ```
   com.example.core.plugin.MockPlugin
   ```

3. Update tests to expect the mock plugin ID instead of real plugin:
   ```java
   assertTrue(pluginLoader.isPluginLoaded("mock-plugin"));
   ```

**Option 2: Integration Test Module**

Create a separate integration test module that depends on both core and implementations:
```
aidrclaw-integration-tests/
  ├── pom.xml (depends on aidrclaw-core + aidrclaw-plugins-*)
  └── src/test/java/...
```

**Option 3: Move SPI Tests**

Move ServiceLoader tests to the implementation module where the SPI implementations exist.

## Verification

After applying Option 1:
```bash
mvn clean test -pl aidrclaw-core -am
```

Expected output:
- Tests run: N, Failures: 0, Errors: 0
- ServiceLoader finds and loads mock plugin
- No circular dependency errors

## Example

**Project Structure:**
```
aidrclaw-parent/
  ├── aidrclaw-plugin-api/     # SPI interfaces
  ├── aidrclaw-core/           # ServiceLoader, PluginLoader
  └── aidrclaw-plugins-storage/ # SPI implementation (depends on core)
```

**Mock Plugin:**
```java
// src/test/java/com/aidrclaw/core/plugin/MockPlugin.java
public class MockPlugin implements Plugin {
    @Override
    public void init(PluginContext context) { }
    
    @Override
    public PluginResult execute(PluginContext context) {
        return PluginResult.success();
    }
    
    @Override
    public void destroy() { }
    
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata() {
            public String getPluginId() { return "mock-plugin"; }
            public String getVersion() { return "1.0.0"; }
            public String getName() { return "Mock 插件"; }
            public String getDescription() { return "用于测试的 Mock 插件"; }
        };
    }
}
```

**SPI Configuration:**
```
# src/test/resources/META-INF/services/com.aidrclaw.plugin.Plugin
com.aidrclaw.core.plugin.MockPlugin
```

## Notes

- Mock plugins should implement the full SPI interface but with minimal logic
- Use distinct plugin IDs to avoid confusion with production plugins
- Document why mock exists (test-only, avoids circular dependency)
- For integration testing, use Option 2 to test with real implementations
- This pattern applies to any Java SPI usage, not just ServiceLoader

## References

- [Java SPI Mechanism](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
- [Maven Multi-Module Best Practices](https://maven.apache.org/guides/mini/guide-multiple-modules.html)
- [Testing SPI Implementations](https://stackoverflow.com/questions/2471638/how-to-test-spi-implementation)
