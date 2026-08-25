# CWE-321 Vulnerability Remediation Plan

**Session ID**: 20c84525-bda5-42de-bd83-173a62aa2c0c  
**Date**: 2026-08-26  
**Branch**: `modernize/java-20260824144329`  
**Language**: Java  
**Scenario**: Scan and resolve CWE-321 (Use of Hard-coded Cryptographic Key) vulnerabilities.

## CWE-321 Definition

CWE-321: Use of Hard-coded Cryptographic Key — the product contains a cryptographic key or secret that is embedded directly in the source code, making it impossible to change without modifying and recompiling the software.

## Findings

### Finding 1 — Hard-coded fallback salt in `PBEWithMD5AndDESEncoder` (MAIN SOURCE CODE)

**File**: `identity4j-utils/src/main/java/com/identity4j/util/crypt/impl/PBEWithMD5AndDESEncoder.java`  
**Lines**: 49-51 (DEFAULT_SALT), 118 (usage), 93-100 (main() passphrase)

**Issues**:
1. `DEFAULT_SALT` — a `private final static byte[]` array is used as a fallback PBE salt when the caller passes `null`. This makes all cipher-texts produced with a null salt use an identical, publicly-known salt, weakening PBKDF derivation.
2. `main()` method — contains `new Crypt("A passphrase".toCharArray(), null)`, embedding a literal passphrase directly into the cryptographic key derivation call.

**Fix**:
- Remove `DEFAULT_SALT` constant.
- In `Crypt(char[] pass, byte[] salt)` constructor: when `salt == null`, generate a cryptographically-random 8-byte salt using `SecureRandom` instead of using the static fallback.
- Remove the `main()` method entirely (dead demo code).
- Backward compatibility is maintained because encoded data stores the actual salt bytes in the output payload; `decode()` reads the salt from the payload, not from `DEFAULT_SALT`.

### Finding 2 — Hard-coded passphrase in disabled test `Base64FIPSEncoderTestDISABLED`

**File**: `identity4j-utils/src/test/java/com/identity4j/util/crypt/impl/Base64FIPSEncoderTestDISABLED.java`  
**Line**: 40

**Issue**: `byte[] passphrase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456".getBytes("US-ASCII");` — a fixed, well-known passphrase is used to seed the NSS token database as a cryptographic key.

**Fix**: Replace with a `SecureRandom`-generated passphrase of the same length. Since this test generates its expected hashes dynamically (the expected hash comparison is already based on the previously stored encoded values from previous encoding), this will break the static expected hash values. As the test is permanently disabled (class name suffix `DISABLED` and is excluded from surefire), it cannot be run and the best fix is to document the hardcoded key and use a runtime-generated passphrase.

## Not CWE-321 (Excluded)

- `BCryptKDF.java` `"OxychromaticBlowfishSwatDynamite"` — standardized public algorithm constant from the bcrypt-pbkdf specification (Ted Unangst / OpenBSD). Not a secret key.
- `PBEWithMD5AndDESEncoder` constant `COUNT = 17` — iteration count, not a key.
- Configuration constant strings like `GOOGLE_PRIVATE_KEY_PASSPHRASE`, `SSH_SERVICE_ACCOUNT_PRIVATE_KEY_PASSPHRASE` — these are property-key names used to look up runtime-provided secrets, not the secrets themselves.

## Files to Change (Dependency Order)

1. `identity4j-utils/src/main/java/com/identity4j/util/crypt/impl/PBEWithMD5AndDESEncoder.java`
2. `identity4j-utils/src/test/java/com/identity4j/util/crypt/impl/Base64FIPSEncoderTestDISABLED.java`

## Build Environment

- **JDK**: 21 (OpenJDK)
- **Build tool**: Maven
- **JAVA_HOME**: detected at runtime by #appmod-build-java-project
