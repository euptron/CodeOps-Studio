# Contributing to CodeOps Studio

Thank you for your interest in contributing to CodeOps Studio! By contributing, you agree to license
your work under the [GNU GPLv3](./LICENSE) under same terms.

We value your input and strive to make the contribution process straightforward and transparent.
This project uses [semantic version `2.0.0` spec](https://semver.org/spec/v2.0.0.html) in the format
`major.minor.patch(-prerelease)(+buildmetadata)`. When bumping `versionName` in `constants.gradle`
file and when creating a tag for new releases on github, make sure to include the patch number as
well, like `v1.1.0` instead of just `v1.1`. The `build.gradle` files for consistency.

## Ways to Contribute

You can contribute to CodeOps Studio in several ways:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

All these can be done by [opening an issue](https://github.com/euptron/CodeOps-Studio/issues)

## Development Platform

We use GitHub to host our codebase, track issues, and manage pull requests. All contributions are
managed through GitHub's pull request system.

## Contribution Process

**Branch Policy:**

- `main`: Stable releases (protected)
- `dev`: Active development

To contribute to CodeOps Studio, follow these steps:

1. **Fork the Repository:** Start by forking this repository to your GitHub account.

2. **Create a Branch:** Create a new branch from the `dev` branch for your feature or bug fix.

3. **Implement Changes:** Make your changes in the branch you created, ensuring each commit has a
   clear and descriptive message and **must**
   use [Conventional Commits](https://www.conventionalcommits.org) specs like:
    - **Added** for new features.
    - **Changed** for changes in existing functionality.
    - **Deprecated** for soon-to-be removed features.
    - **Removed** for now removed features.
    - **Fixed** for any bug fixes.
    - **Security** in case of vulnerabilities.
    - **Docs** for updating documentation.

4. **Submit a Pull Request:** Open a pull request (PR) against the `dev` branch of this repository.
   Provide a detailed description of the changes you've made and the problem they solve.

5. **License Notice:** Ensure that any new files you add include the GPLv3 license header, you can
   use the [automation script](./docs/add_license_header.md) to add license headers to new files.

6. **Code Review:** Your PR will undergo review by maintainers. Address any feedback or requested
   changes promptly.

## Code Standards

- If you've added code, ensure it is tested thoroughly.
- Update documentation for any changes in APIs or significant features.
- Ensure the code follows our style guidelines and passes linting checks.

## Bug Reporting

We use GitHub issues to track bugs. If you encounter a bug, please report it
by [opening a new issue](https://github.com/euptron/CodeOps-Studio/issues). Include detailed steps
to reproduce the bug, expected behavior, actual behavior observed, and any relevant code snippets.


## Versioning Policy (Required)

CodeOps Studio uses a **strict, enforced variation of Semantic Versioning** that is intentionally designed to support:

* deterministic update checks
* forced minimum-version enforcement
* Android `versionCode` constraints
* Git-based build traceability
* reliable client/server version comparison

This policy is **not optional**. Any deviation may cause update logic to fail silently or force incorrect downgrade/upgrade behavior.

---

### Version Format

The application version **MUST** follow this structure:

```
MAJOR.MINOR.PATCH [RELEASE_TYPE]
```

Examples:

* `1.0.5`
* `1.0.5 beta`
* `1.2.0 rc`
* `2.0.0 stable`

Internally, spaces are normalized and treated as `-` during comparison.

---

### Core Version (Mandatory)

The **core version**:

```
MAJOR.MINOR.PATCH
```

* MUST contain **exactly three numeric segments**
* MUST be strictly numeric
* MUST NOT include prefixes (`v`), suffixes, or metadata
* MUST increase monotonically

Valid:

* `1.0.0`
* `1.2.3`

Invalid:

* `1.0`
* `1.0.0.1`
* `v1.0.0`
* `1.0.0-beta` (labels are handled separately)

The core version is used for **primary ordering** during version comparison.

---

### Release Types (Controlled Vocabulary)

Release maturity is expressed using a **single release-type label**, mapped to a strict priority order.

| Release Type | Key      | Priority |
| ------------ | -------- | -------- |
| Alpha        | `alpha`  | 1        |
| Beta         | `beta`   | 2        |
| RC           | `rc`     | 3        |
| Pre-release  | `pr`     | 4        |
| Stable       | `stable` | 5        |

Ordering rule:

```
alpha < beta < rc < pr < stable
```

Only these labels are recognized.

---

### Release Type Rules

* The release type:

  * MUST be a **single word**
  * MUST NOT contain punctuation
  * MUST NOT contain dashes, commas, or numbers
* Only one release type may be present
* If omitted, the version is treated as **STABLE**

Valid:

* `1.0.5 beta`
* `1.0.5 rc`
* `1.0.5`

Invalid:

* `1.0.5-beta-2`
* `1.0.5 beta1`
* `1.0.5 preview`
* `1.0.5 final`

---

### Why This Is Strictly Enforced

CodeOps Studio uses a **custom version comparator** to determine:

* update availability
* forced update requirements
* backward compatibility guarantees

The comparator works in **two phases**:

1. **Numeric comparison** of `MAJOR.MINOR.PATCH`
2. **Priority comparison** of release type

Because Android update logic depends on this ordering:

* malformed versions may appear *newer* than they are
* stable releases may be downgraded incorrectly
* forced updates may never trigger
* CI and Play Store builds may diverge

This is why:

* free-form SemVer prerelease identifiers are not allowed
* build metadata is intentionally ignored
* release types are finite and ordered

---

### Git Integration and Version Code

* `versionCode` is derived from **Git commit count**
* A fallback monotonic value is used when Git is unavailable
* Contributors **must not** manually override `versionCode`

This guarantees:

* Play Store–safe monotonic upgrades
* reproducible builds
* traceable releases

---

### What Contributors Are Allowed to Change

Only the following versioning fields may be modified when explicitly required:

* `baseVersion` (core semantic version only)
* `suffix` (release type only)

Contributors **must not**:

* manually construct `versionName`
* append commit hashes themselves
* introduce additional version labels
* add formatting, symbols, or separators

---

### Tagging Releases

Git tags **MUST** match the core version:

```
vMAJOR.MINOR.PATCH
```

Examples:

* `v1.0.5`
* `v2.1.0`

Tags must not include release types or metadata.

---

### Violations

Pull requests that:

* introduce unsupported version formats
* modify version comparison logic
* bypass version normalization
* add unrecognized release labels

**will be rejected**.

This policy exists to protect update correctness and user safety.

## Licensing

By contributing to CodeOps Studio, you agree that your contributions will be licensed under
the [GNU General Public License v3  (GPLv3)](https://choosealicense.com/licenses/gpl-3.0/). For any
concerns about licensing, please reach out to the maintainers.

Report violations to the [GNU GPLv3 Licence](./LICENSE) by opening
an [issue](https://github.com/euptron/CodeOps-Studio/issues)

## Acknowledgment

This contribution guide draws inspiration from various open-source best practices. We aim to foster
a welcoming and collaborative community where contributions are valued.

We look forward to your contributions to CodeOps Studio!
