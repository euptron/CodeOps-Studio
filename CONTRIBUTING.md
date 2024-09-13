# Contributing to CodeOps Studio

Thank you for your interest in contributing to CodeOps Studio! We value your input and strive to make the contribution process straightforward and transparent.
This project uses [semantic version `2.0.0` spec](https://semver.org/spec/v2.0.0.html) in the format `major.minor.patch(-prerelease)(+buildmetadata)`. When bumping `versionName` in `constants.gradle` file and when creating a tag for new releases on github, make sure to include the patch number as well, like `v1.1.0` instead of just `v1.1`. The `build.gradle` files for consistency.

## Ways to Contribute

You can contribute to CodeOps Studio in several ways:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## Development Platform

We use GitHub to host our codebase, track issues, and manage pull requests. All contributions are managed through GitHub's pull request system.

## Contribution Process

To contribute to CodeOps Studio, follow these steps:

1. **Fork the Repository:** Start by forking this repository to your GitHub account.

2. **Create a Branch:** Create a new branch from the `main` branch for your feature or bug fix.

3. **Implement Changes:** Make your changes in the branch you created, ensuring each commit has a clear and descriptive message and **must** use [Conventional Commits](https://www.conventionalcommits.org) specs like:
      - **Added** for new features.
      - **Changed** for changes in existing functionality.
      - **Deprecated** for soon-to-be removed features.
      - **Removed** for now removed features.
      - **Fixed** for any bug fixes.
      - **Security** in case of vulnerabilities.
      - **Docs** for updating documentation.

4. **Submit a Pull Request:** Open a pull request (PR) against the `main` branch of this repository. Provide a detailed description of the changes you've made and the problem they solve.

5. **License Notice:** Ensure that any new files you add include the GPLv3 license header.

6. **Code Review:** Your PR will undergo review by maintainers. Address any feedback or requested changes promptly.

## Code Standards

- If you've added code, ensure it is tested thoroughly.
- Update documentation for any changes in APIs or significant features.
- Ensure the code follows our style guidelines and passes linting checks.

## Bug Reporting

We use GitHub issues to track bugs. If you encounter a bug, please report it by [opening a new issue](https://github.com/euptron/CodeOps-Studio/issues). Include detailed steps to reproduce the bug, expected behavior, actual behavior observed, and any relevant code snippets.

## Licensing

By contributing to CodeOps Studio, you agree that your contributions will be licensed under the [GNU General Public License v3  (GPLv3)](https://choosealicense.com/licenses/gpl-3.0/). For any concerns about licensing, please reach out to the maintainers.

## Acknowledgment

This contribution guide draws inspiration from various open-source best practices. We aim to foster a welcoming and collaborative community where contributions are valued.

We look forward to your contributions to CodeOps Studio!

