# CWE-23 Relative Path Traversal Remediation — Plan

**Session ID**: 4fa6aab7-4aaf-4e37-b31b-dc9fa88bf617  
**Created**: 2026-08-26  
**Branch**: `modernize/java-20260824144329`  
**Language**: Java (Maven multi-module)

---

## Migration Scenario

Scan the identity4j codebase for CWE-23 (Relative Path Traversal) vulnerabilities — specifically
cases where user-influenced relative paths (containing `../`) can escape an intended base directory
— and remediate all confirmed instances.

CWE-23 is a specialisation of CWE-22 (Path Traversal) restricted to *relative* path sequences
(`../`, `..\`, URL-encoded equivalents).

---

## Build Environment

- **JDK Version**: 21 (OpenJDK)
- **Build Tool**: Maven (system mvn)
- **JAVA_HOME**: detected from system (OpenJDK 21)

---

## Scan Results

### Files with file I/O examined

| File | Input source | CWE-23? |
|---|---|---|
| `identity4j-utils/…/Util.java` `unzip()` | ZipEntry names from external ZIP | **Fixed by CWE-22 session** |
| `identity4j-utils/…/IOUtil.java` `getStreamFromResource()` | `resourceName` parameter | Callers always pass `res://` classpath refs — not exploitable |
| `identity4j-connector/…/AbstractRestWebServiceConnectorTest.java` | `propertiesFile` test param | Test-only; not production code |
| `identity4j-script-hypersocket/…/HypersocketConfiguration.java` | admin config file path | Admin-controlled; not user-supplied |
| `identity4j-unix/…/UnixConnector.java` | file paths | Hardcoded system paths only |
| `identity4j-sap/…/Lib.java` | library paths | Hardcoded platform strings from `getAO()` |
| `identity4j-flatfile` connectors | file URI | Admin-configured VFS URI |
| `identity4j-script-ssh` | remote file paths | Hardcoded `/etc/shadow`, `/etc/passwd` |

### Confirmed CWE-23 vulnerability

**`Util.unzip()`** — already remediated in CWE-22 session (commit `73bb0ea`).

The fix uses two guards:
1. Fast-fail: reject absolute-path entries before `File` construction.
2. Canonical-path check: `file.getCanonicalPath().startsWith(canonicalDest)` catches any
   relative traversal (`../../`, `subdir/../../../`, etc.).

No additional CWE-23 vulnerabilities were found in production code.

---

## Work Items

| # | Task | File | Status |
|---|---|---|---|
| 1 | Confirm existing CWE-22/23 fix in `Util.unzip()` | `Util.java` | ✅ already fixed |
| 2 | Add dedicated CWE-23 regression tests | `UtilCwe23Test.java` (new) | ✅ done |

---

## Dependency Order

`Util.java` has no inbound dependencies on other changed files and is patched first.
`UtilCwe23Test.java` depends only on `Util.unzip()`.
