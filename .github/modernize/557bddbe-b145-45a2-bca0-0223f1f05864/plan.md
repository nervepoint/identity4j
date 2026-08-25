# CWE-821 Migration Plan

## Session
- **Session ID**: 557bddbe-b145-45a2-bca0-0223f1f05864
- **Timestamp**: 2026-08-25
- **Language**: Java
- **Branch**: modernize/java-20260824144329
- **JDK**: OpenJDK 21 (target)

## Build Environment
- **JDK version**: 21
- **Build tool**: Maven

## CWE-821 Overview
CWE-821 (Incorrect Synchronization) occurs when a program uses a shared resource without proper synchronization.
Primary pattern fixed: **Volatile field TOCTOU race** — a volatile object field is read for a null check in one expression, then read AGAIN in the next expression. Between the two reads, another thread may set the field to null, causing an NPE.

The fix is the **snapshot pattern**: capture the volatile field in a thread-local variable once, then use that variable exclusively within the method/block.

## Files to Change (in dependency order)

```json
{
  "filesToBeChanged": [
    "identity4j-script-ssh/src/main/java/com/identity4j/connector/script/ssh/SshConnector.java",
    "identity4j-vfs/src/main/java/com/identity4j/connector/vfs/AbstractVFSConnector.java",
    "identity4j-as400/src/main/java/com/identity4j/connector/as400/As400Connector.java",
    "identity4j-connector/src/main/java/com/identity4j/connector/util/DummySSLSocketFactory.java",
    "identity4j-ldap-directory-jndi/src/main/java/com/identity4j/connector/jndi/directory/AbstractDirectoryConnector.java",
    "identity4j-unix/src/main/java/com/identity4j/connector/unix/UnixConnector.java"
  ],
  "kbId": null
}
```

## Vulnerabilities & Fixes

### 1. SshConnector.java — CRITICAL
- **Issue**: `isOpen()` reads volatile `client` field 3 times: null check + `isConnected()` + `isAuthenticated()`. `disconnect()` (synchronized) can set `client = null` between any two reads, causing NPE.
- **Fix**: Capture `client` in local variable (`SshClientWrapper c = client`).

### 2. AbstractVFSConnector.java — HIGH
- **Issue**: `isReadOnly()` calls `file.isWriteable()` without null check. If another thread closes the connector (setting `file = null`), NPE occurs.
- **Fix**: Capture `file` snapshot and add null guard.

### 3. As400Connector.java — HIGH  
- **Issue**: `isOpen()` reads volatile `as400` twice: `as400 != null && as400.isConnected()`. Race window between the two reads.
- **Fix**: Capture `as400` snapshot.

### 4. DummySSLSocketFactory.java — HIGH
- **Issue**: `getSupportedCipherSuites()` calls `getIncludeCipherSuites()` 3 times and `getExcludeCipherSuites()` 2 times. These return the volatile array each time. If another thread nullifies the array between calls, NPE or wrong behavior.
- **Fix**: Capture both arrays in local variables at method entry.

### 5. AbstractDirectoryConnector.java — HIGH
- **Issue 1**: `setSocketFactory()` checks `ldapService != null` then calls `ldapService.setSocketFactory()` — two separate volatile reads.
- **Issue 2**: `onClose()` calls `ldapService.close()` without null guard — if called concurrently, second call NPEs.
- **Fix**: Snapshot pattern for both methods.

### 6. UnixConnector.java — HIGH
- **Issue**: `lockIdentity()`, `unlockIdentity()`, `onSetPassword()`, and `updateUserRow()` read volatile `passwordsInShadow` and `shadowFlatFile` in separate operations. If `checkShadowLoaded()` runs between these reads, state may be inconsistent.
- **Fix**: Capture both `passwordsInShadow` and `shadowFlatFile` in local variables at the start of each method.
