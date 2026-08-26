# CWE-22 Path Traversal Remediation — Plan

**Session ID**: c8a31b22-70ae-407a-afea-3351c2a6ad95  
**Created**: 2026-08-26  
**Branch**: `modernize/java-20260824144329`  
**Language**: Java (project uses Maven multi-module build)

---

## Migration Scenario

Scan the identity4j codebase for CWE-22 (Path Traversal) vulnerabilities — primarily Zip Slip and
file-path construction from unvalidated input — and remediate all confirmed instances.

---

## Knowledge Base

Search scenario: "CWE-22 path traversal zip slip Java canonical path"

---

## Build Environment

- **JDK Version**: 21 (OpenJDK; present in JAVA_HOME)
- **Build Tool**: Maven (mvn wrapper or system maven)
- **JAVA_HOME**: detected from system

---

## Files Changed

1. `identity4j-utils/src/main/java/com/identity4j/util/Util.java` (production fix)
2. `identity4j-utils/src/test/java/com/identity4j/util/UtilCwe22Test.java` (regression test)

Fix committed in: `73bb0ea` ("Step 3: Commit pending security improvements")

---

## Dependency Order

The `Util` class has no inbound dependencies on other changed files, so it was patched first.
The test file depends only on `Util.unzip()`.

---

## Fix Summary

### `Util.unzip()` — Zip Slip Guard

**Before (vulnerable):**
```java
File file = new File(directory, name);
file.getParentFile().mkdirs();
// no canonical-path check — entry could escape target directory
```

**After (fixed):**
```java
// Step 1 — fast-fail: reject absolute-path entries before File construction
String name = entry.getName();
if (name.startsWith("/") || name.startsWith("\\") || name.matches("^[A-Za-z]:[/\\\\].*")) {
    throw new IOException("Zip Slip detected: absolute entry path rejected: " + name);
}
// Step 2 — canonical-path guard: reject traversal sequences like ../../
File file = new File(directory, name);
if (!file.getCanonicalPath().startsWith(canonicalDest + File.separator)
        && !file.getCanonicalPath().equals(canonicalDest)) {
    throw new IOException("Zip Slip detected: entry escapes target directory: " + name);
}
```

### `UtilCwe22Test` — Regression Tests

Three JUnit 4 test cases:
1. `normalEntryExtractsSuccessfully` — happy path, `subdir/file.txt`
2. `zipSlipEntryIsRejected` — traversal entry `../../evil.txt` must throw `IOException`
3. `absolutePathEntryIsRejected` — absolute entry `/tmp/evil-absolute.txt` must throw `IOException`
