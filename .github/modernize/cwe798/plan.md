# CWE-798 Migration Plan

## Session
- **Session ID**: bdde6987-e586-485e-876a-9aaa85139f56
- **Timestamp**: 2026-08-26
- **Target Branch**: `modernize/java-20260824144329`
- **Baseline Commit**: b59e9da

## Language
Java

## Scenario
Scan and resolve CWE-798 (Use of Hard-coded Credentials) vulnerabilities.

## Summary of Findings

Previous tasks on this branch already remediated:
- **CWE-259**: Hardcoded passwords in most test properties files (commit b3fc501)
- **CWE-321**: Hardcoded cryptographic keys in BCrypt.java (commit b48f857)

### Remaining Active CWE-798 Vulnerabilities

| File | Issue | Severity |
|------|-------|----------|
| `identity4j-google/src/test/resources/google-connector.properties` | Embedded RSA 2048-bit private key for a Google service account; also exposes `private_key_id`, `client_email`, `client_id`, and real Google account email | **Critical** |
| `identity4j-office365/src/test/resources/office365-connector.properties` | Hardcoded `office365SymmetricKey=5Cd.WhkeyHdV3-Ro2_OCu96F.a_7d9fYTL` (active) and a second commented-out symmetric key | **High** |

## Build Environment
- **JDK version**: 11 (determined from pom.xml)
- **Build tool**: Maven (pom.xml)

## Files to Be Changed

1. `identity4j-google/src/test/resources/google-connector.properties`
   - Replace `googleServiceAccountJson` with a placeholder JSON (no real private key)
   - Replace `googleUsername` with `REPLACE_WITH_ACTUAL`

2. `identity4j-office365/src/test/resources/office365-connector.properties`
   - Replace active `office365SymmetricKey` value with `REPLACE_WITH_ACTUAL`
   - Replace commented-out `office365SymmetricKey` value with `REPLACE_WITH_ACTUAL`

## Remediation Strategy
Replace hardcoded credentials with `REPLACE_WITH_ACTUAL` placeholders. For the Google service account JSON, replace the entire JSON value with a well-formed placeholder JSON indicating each field must be configured at runtime.
