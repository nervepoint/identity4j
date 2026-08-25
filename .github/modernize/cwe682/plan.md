# CWE-682 Remediation Plan

**Session ID**: 5142e04f-7415-4743-afdc-91a8992b3259  
**Date**: 2026-08-25  
**Branch**: `modernize/java-20260824144329`  
**Language**: Java  

## Build Environment

- **JDK Version**: OpenJDK 21 (LTS)  
- **JAVA_HOME**: /usr/lib/jvm/java-21-semeru-openj9-amd64  
- **Build Tool**: Maven  
- **MAVEN_HOME**: (mvn on PATH)

## Scenario

Scan and resolve CWE-682 (Incorrect Calculation) vulnerabilities in the identity4j multi-module Maven project.

## CWE-682 Vulnerabilities Found

| # | File | Line | Pattern | Severity |
|---|------|------|---------|----------|
| 1 | `identity4j-zendesk/.../Token.java` | 110 | `(diff/1000 % 60)/60` always 0 — `hasPassed()` is always false | High |
| 2 | `identity4j-salesforce/.../Token.java` | 106 | `diff/1000 % 60` gives seconds-within-minute (0-59) instead of total seconds | High |
| 3 | `identity4j-active-directory-jndi/.../ActiveDirectoryConnector.java` | 877 | `PASSWD_NOTREQD_FLAG ^ Integer.MAX_VALUE` incorrect bitmask — also clears sign bit | Medium |
| 4 | `identity4j-active-directory-jndi/.../ActiveDirectoryConnector.java` | 1539 | `lockoutDuration / 1000` (100-ns→μs) instead of `/ 10000L` (100-ns→ms) | High |
| 5 | `identity4j-office365/.../ADToken.java` | 185 | Subtracts non-zero `epochMillis` (current time-of-day in 1970) — expiry check off by up to 24 h | Medium |

## Files to Change (dependency order)

1. `identity4j-zendesk/src/main/java/com/identity4j/connector/zendesk/services/token/handler/Token.java`
2. `identity4j-salesforce/src/main/java/com/identity4j/connector/salesforce/services/token/handler/Token.java`
3. `identity4j-active-directory-jndi/src/main/java/com/identity4j/connector/jndi/activedirectory/ActiveDirectoryConnector.java`
4. `identity4j-office365/src/main/java/com/identity4j/connector/office365/services/token/handler/ADToken.java`
