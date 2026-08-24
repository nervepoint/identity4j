# Modernization Plan: Identity4J Java and Security Modernization

**Project**: identity4j
**Assessment source**: `assessment/reports/report-20260824124049/report.json`
**Assessment date**: 2026-08-24

## Technical Framework

- **Language**: Java, currently compiled for Java 8; target OpenJDK 21
- **Framework**: Java EE/Jakarta EE APIs; target latest stable Jakarta EE
- **Build Tool**: Multi-module Maven reactor with Eclipse project metadata
- **Project Shape**: 24 connector and utility modules
- **Security Scope**: 22 selected CWE findings and 107 CVE/GHSA findings

## Overview

This plan modernizes only the categories selected from assessment report
`report-20260824124049`. It upgrades the supported Java and Jakarta EE baselines,
completes the Maven configuration while preserving all Eclipse metadata,
replaces the three selected deprecated API families, remediates the 22 requested
CWE classes, and scans and fixes all dependency vulnerabilities recorded by the
assessment.

No new assessment, infrastructure provisioning, deployment, containerization,
or integration-test phase is included.

## Selected Assessment Scope

| Category | Selected issue | Assessment evidence |
|---|---|---:|
| Framework Upgrade (Java EE/Jakarta EE) | Jakarta EE Version is not the latest stable | 25 incidents |
| Java Version Upgrade | Java Version Has Reached the End of Support | 2 incidents |
| Build Tool (Eclipse) | Eclipse project found | 86 incidents |
| Deprecated APIs | Three selected deprecated API rules | 12 incidents |
| CWE remediation | 22 explicitly selected CWE identifiers | 22 findings |
| CVE remediation | All CVE/GHSA findings in the report | 107 findings |

The assessment's CWE-89 and CWE-502 findings are excluded because they were not
part of the selected scope.

## Execution Phases

1. Upgrade the Java runtime baseline to OpenJDK 21.
2. Upgrade the Java EE/Jakarta EE baseline to the latest stable release.
3. Replace the selected deprecated Java APIs.
4. Complete the Eclipse-to-Maven project migration.
5. Scan and remediate the 22 selected CWE findings.
6. Scan and remediate all reported dependency CVE/GHSA findings.

Tasks are serialized in this order because the upgrade, build metadata, source,
and dependency changes overlap across the same Maven reactor. Each task must
leave the full reactor buildable and its unit tests passing before the next task
starts.

## Migration Impact Summary

| Application | Current state | Target state | Comments |
|---|---|---|---|
| identity4j | Java 8 compiler baseline | OpenJDK 21 | Preserve public behavior |
| identity4j | Legacy Java EE APIs | Latest stable Jakarta EE | Update affected modules |
| identity4j | Maven plus Eclipse metadata | Complete Maven configuration plus unchanged Eclipse metadata | 49 metadata files found |
| identity4j | Deprecated APIs | Supported replacements | Three selected API families |
| identity4j | Selected CWE/CVE findings | Findings remediated | Verify build and tests |

## Success Criteria

- The multi-module Maven reactor compiles on OpenJDK 21.
- All existing unit tests pass after every task.
- The 25 Jakarta EE incidents are resolved against the latest stable release.
- Maven configuration represents all required source, resource, generated-source,
  and dependency settings without modifying, deleting, renaming, or regenerating
  any `.project` or `.classpath` file; every such file remains unchanged.
- The 12 selected deprecated API incidents are resolved.
- Re-scanning the selected 22 CWE identifiers reports no unresolved findings.
- Dependency scanning reports no remediable CVE/GHSA findings at the assessment's
  medium-or-higher threshold; accepted exceptions are documented with rationale.

## Constraints and Blockers

- Planning blocker: none.
- Execution must verify third-party connector compatibility with OpenJDK 21 and
  the selected Jakarta EE release before removing compatibility dependencies.
- CVE fixes that require major dependency upgrades must document breaking-change
  risk and may require follow-up compatibility work within the CVE task.
- Findings under `identity4j-office365/old/` remain in scope where explicitly
  identified by a selected CWE.

## Open Questions & Questionnaire

- [x] Q: Provision infrastructure? A: No; this is code modernization only.
- [x] Q: Include integration testing? A: No; exact selected scope only.
- [x] Q: Include security and CVE remediation? A: Yes; use the explicit scope.
- [x] Q: Select a deployment target? A: No deployment.
- [x] Q: Include containerization? A: No.
