# CWE-477 Remediation Migration Plan

**Session ID**: 3eceed6f-be2c-4f2a-94fe-7d50dff083fc  
**Workspace**: /home/SOUTHPARK/tanktarta/Documents/Git/identity4j  
**Language**: Java (confirmed – pom.xml with `<source>21</source><target>21</target>`)  
**Scenario**: Scan and resolve CWE-477 vulnerabilities  
**Branch**: modernize/java-20260824144329  
**Created**: 2026-08-25  
**Baseline Commit**: 1e49674  

---

## Build Environment

### JDK Settings
- JDK version used by project: **21** (`maven-compiler-plugin` source/target = 21)
- Need to install new JDK: TBD (check existing JDKs)
- JAVA_HOME: TBD (auto-detect)
- Build tool: **Maven** (wrapper not used – use system Maven)

---

## CWE-477 Findings

CWE-477 = **Use of Obsolete Function**: deprecated constructors, deprecated APIs, 
charset-string-based encoding APIs (String charset arg throws checked exception unnecessarily; 
Charset-based overloads are the preferred replacement since Java 10).

---

## Files to be Changed (dependency order)

### 1. `identity4j-utils/src/main/java/com/identity4j/util/Extender.java`
- **Issue**: `new Integer(order).compareTo(new Integer(other.order))` (line ~140)
- **Fix**: Replace with `Integer.compare(order, other.order)`

### 2. `identity4j-active-directory-jndi/src/main/java/com/identity4j/connector/jndi/activedirectory/ActiveDirectoryUtils.java`
- **Issue**: `new Long(Long.parseLong(rid, 16))` (line ~75)
- **Fix**: Replace with `Long.valueOf(Long.parseLong(rid, 16))`

### 3. `identity4j-jdbc/src/main/java/com/identity4j/connector/jdbc/NamedParameterStatement.java`
- **Issue**: `new Integer(index)` (line ~120)
- **Fix**: Replace with `Integer.valueOf(index)`

### 4. `identity4j-mysql/src/main/java/com/identity4j/connector/mysql/MySQLConfiguration.java`
- **Issue**: `new Integer(3306)` (line ~65)
- **Fix**: Replace with `Integer.valueOf(3306)`

### 5. `identity4j-mysql-users-connector/src/main/java/com/identity4j/connector/mysql/users/MySQLUsersConfiguration.java`
- **Issue**: `new Integer(3306)` (line ~87)
- **Fix**: Replace with `Integer.valueOf(3306)`

### 6. `identity4j-aws/src/main/java/com/identity4j/connector/aws/NameValuePair.java`
- **Issue**: `URLDecoder.decode(element.substring(idx+1), "UTF-8")` throws `UnsupportedEncodingException`
- **Fix**: Use `URLDecoder.decode(element.substring(idx+1), StandardCharsets.UTF_8)` + remove try-catch

### 7. `identity4j-office365/src/main/java/com/identity4j/connector/office365/Office365OAuth.java`
- **Issue**: `URLEncoder.encode(str, "UTF-8")` with String charset (throws `UnsupportedEncodingException`)
- **Fix**: Use `URLEncoder.encode(str, StandardCharsets.UTF_8)` + remove try-catch + import

### 8. `identity4j-office365/src/main/java/com/identity4j/connector/office365/services/UserService.java`
- **Issue**: `URLEncoder.encode(filter.encode(), "UTF-8")` with String charset (throws `UnsupportedEncodingException`)
- **Fix**: Use `URLEncoder.encode(filter.encode(), StandardCharsets.UTF_8)` + remove try-catch + import

### 9. `identity4j-script-http/src/main/java/com/identity4j/connector/script/http/HttpClientWrapper.java`
- **Issue 1**: `new JsonParser().parse(str)` – deprecated Gson API
- **Fix 1**: `JsonParser.parseString(str)`
- **Issue 2**: `new String(resp.content(), "UTF-8")` – String-based charset throws `UnsupportedEncodingException`
- **Fix 2**: `new String(resp.content(), StandardCharsets.UTF_8)` + remove checked exception + add import

---

## Knowledge Base
- No specific KB ID; pattern-based CWE-477 remediation using Java 21 best practices.

---

```json
{
  "filesToBeChanged": [
    "identity4j-utils/src/main/java/com/identity4j/util/Extender.java",
    "identity4j-active-directory-jndi/src/main/java/com/identity4j/connector/jndi/activedirectory/ActiveDirectoryUtils.java",
    "identity4j-jdbc/src/main/java/com/identity4j/connector/jdbc/NamedParameterStatement.java",
    "identity4j-mysql/src/main/java/com/identity4j/connector/mysql/MySQLConfiguration.java",
    "identity4j-mysql-users-connector/src/main/java/com/identity4j/connector/mysql/users/MySQLUsersConfiguration.java",
    "identity4j-aws/src/main/java/com/identity4j/connector/aws/NameValuePair.java",
    "identity4j-office365/src/main/java/com/identity4j/connector/office365/Office365OAuth.java",
    "identity4j-office365/src/main/java/com/identity4j/connector/office365/services/UserService.java",
    "identity4j-script-http/src/main/java/com/identity4j/connector/script/http/HttpClientWrapper.java"
  ],
  "kbId": null
}
```
