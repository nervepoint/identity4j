# CWE-611 Remediation Plan

**Session ID**: 7be2a473-c734-470a-bf78-00901259c5a9  
**Date**: 2026-08-26  
**Branch**: `modernize/java-20260824144329`  
**Workspace**: /home/SOUTHPARK/tanktarta/Documents/Git/identity4j

## Summary

CWE-611 – Improper Restriction of XML External Entity Reference (XXE).  
Two files were identified in the assessment; both fixes were already committed
in commit `73bb0ea` (Step 3: Commit pending security improvements).

## Vulnerability Evidence

| File | Line | Issue |
|------|------|-------|
| `identity4j-utils/src/main/java/com/identity4j/util/xml/XMLDataExtractor.java` | 71 | `XMLInputFactory.newInstance()` without `IS_SUPPORTING_EXTERNAL_ENTITIES=false` or `SUPPORT_DTD=false` |
| `identity4j-office365/old/java/com/identity4j/connector/office365/services/token/handler/WindowsLiveLogin.java` | 2013 | `DocumentBuilderFactory.newInstance()` without secure-processing features |

## Build Environment

- **JDK**: Java 21 (LTS) — `/home/SOUTHPARK/tanktarta/.sdkman/candidates/java/21.0.7-sem`
- **Maven**: 3.9.9 — `/home/SOUTHPARK/tanktarta/.sdkman/candidates/maven/3.9.9`
- **Build tool**: Maven (multi-module reactor)
- **Wrapper**: No

## Files Changed

### Already committed in `73bb0ea`

1. `identity4j-utils/src/main/java/com/identity4j/util/xml/XMLDataExtractor.java`  
   – Replaced bare `XMLInputFactory.newInstance()` field initializer with a
     `createSecureXmlInputFactory()` factory method that sets
     `IS_SUPPORTING_EXTERNAL_ENTITIES = false` and `SUPPORT_DTD = false`.

2. `identity4j-office365/old/java/com/identity4j/connector/office365/services/token/handler/WindowsLiveLogin.java`  
   – Added four `factory.setFeature(…, false)` calls and
     `factory.setExpandEntityReferences(false)` in `parseSettings()` before
     `factory.newDocumentBuilder()`.

3. `identity4j-utils/src/test/java/com/identity4j/util/xml/XMLDataExtractorCwe611Test.java` *(new)*  
   – Three regression tests: `externalEntitiesAreDisabled`, `dtdSupportIsDisabled`,
     `wellFormedXmlParsesNormally`.

## Validation Stages

1. Build and Fix
2. CVE Validation
3. Consistency Validation
4. Test Validation
5. Completeness Validation
6. Build Validation (final)

## Knowledge Base

No relevant KB found for direct XXE scenario mapping; fixes applied following
OWASP XXE prevention cheat sheet (disable external entities and DTD for every
XML factory before use).
