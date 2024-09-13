Change Log
==========

All notable changes to this project will be documented in this file, this project uses [semantic versioning `2.0.0` spec](https://semver.org/spec/v2.0.0.html) in the format `major.minor.patch(-prerelease)(+buildmetadata)`.

## TAGS

- `Added`: New features
- `Changes`: Changes in existing functionality
- `Deprecated`: Soon-to-be removed features
- `Removed`: Now removed features
- `Fixed`: Bug fixes
- `Security`: Vulnerabilities
- `Docs`: Documentation updates
- `Contributors`: Release contributors
- `FSC-ID`: Firebase Crashlytics Issue ID

Release 1.0.1 beta
----------------------

_13-09-2024_ (Thursday, Sep 13, 2024)

### Added

* Option to refresh file tree
* AST core module for future Node managment
* Support for `.apk` file installation 
* Progress listener for case conversion in the `ContextualCodeEditor`
* Support for more symbols auto completions `\`„ “ « ‚ ‘ ‹\`

### Changes

* Disable code obfuscation and resource shrinking `minifyEnabled` and `shrinkResources` `= false` because it breaks CodeOps Studio.

### Improvements

 * Optimized file tree loading performance and reduced memory usage
 * JavaDoc improvements
 * Refactored Code base
 * `Pane` class ID generation: shared ID list for all instances of the `Pane` class
 * `Pane` class ID generation method: `Pane#generateUUID()`
 * Optimized `Wizard#getDeviceCountry(Context)` method to retrieve the name of **Country** a device is currently in. Crashlytics now collects the actual **Country Name** instead of the `Device Locale`
 * Revamp detection of invalid last opened project before opening
 * Optimized `IdeApplication` and `CrashActivity` for handling crash intent
 * Revamped `TreeView-TreeNode` children sorting comparator
 * Optimised `ContextualCodeEditor#toLowerCase(String)` and `ContextualCodeEditor#toUpperCase(String)` method by reducing time complexity from O(n^2) to O(n) that was caused by the `string = string + ...` reallocation
 * Case conversion by the `ContextualCodeEditor` is now possible even when `ClipBoard` limit is exceeded
 * Optimized line text duplication by the `CodeEditor`
 * Optimized binary file detection, before a file is flagged as binary we compare how many times it has been flagged as a binary across multiple `check+points` against how many times it didn't, making the whole process more effective.
 
### Fixed

* Fix: Case conversion failure.
    - Summary: Case conversion failed because the result from the `AsyncTask#runNonCancelable` was not parsed into the editor.
    
* Fix: Crash when parsing a string into `ReleaseType(String)` in ChangelogAdapter due to `NullPointerException`
    - Summary: App crashed when trying to load saved release notes due on a null object `ReleaseType(null)` reference in ChangelogAdapter.
    
* Fix: Pane Tab duplication. Proguard actually broke the project so it's disabled until further notice.
    
* Fix: `CodeEditorPane` persistency. `CodeEditorPane` now loads persisted arguments and restores them accordingly without text content history
    
* Fix: Failure to properly detect invalid projects when loading the last opened project.
    - Summary: App crashed as a result of a null file path being passed to the `File.exists` method 
    
* Fix: Crash due to null `Log` object in `Log.equals()` method*
    - Summary: The app crashes with a `NullPointerException` when the `equals()` method is invoked on a null `Log` object in the `Log.equals()` method. This issue arises during item comparisons in the `RecyclerView`, particularly when updating the list using `AsyncListDiffer` in the `LogAdapter`. The crash often occurs when selecting an unsupported `Uri`.
    - [Resolved Issue#10](https://github.com/euptron/CodeOps-Studio/issues/10)
    - FCS-ID: `33308dfde6721aa4e51f9010a1f307ea`
    
* Fix: Crash when accessing a null `CircularProgressIndicator` in `CodeEditorPane.setLoading()`
    - Summary: The app crashed due to a `NullPointerException` when attempting to access a null `CircularProgressIndicator` in the `CodeEditorPane.setLoading()` method. This occurred while loading a file, where the `progressbar` was not properly initialized.
    - [Resolved Issue#9](https://github.com/euptron/CodeOps-Studio/issues/9)
    - FCS-ID: `fcc3d4f7d3825fd148be619cf777948b`
    
* Fix: Crash when accessing null `ContextualCodeEditor` in `CodeEditorPane.canUndo()`  
    - Summary: The app crashed due to a `NullPointerException` when attempting to access a null `ContextualCodeEditor` object in the `CodeEditorPane.canUndo()` method. This occurred during the preparation of the toolbar options menu when the undo state was being checked.
    - [Resolved Issue#8](https://github.com/euptron/CodeOps-Studio/issues/8)
    - FCS-ID: `c6f4d617cc63acaa3fe262dbb1e948ca`
    
* Fix: Crash when accessing a null `ViewHolder` in `RecyclerView`  
    - Summary: App crashed due to a `NullPointerException` when trying to reference a null `ViewHolder` in the `RecyclerView`.
    - [Resolved Issue#7](https://github.com/euptron/CodeOps-Studio/issues/7)
    - FCS-ID: `c12fd425b630739a67de5d35ea207be5`
    
* Fix: JavaScript engine initialization failure. The issue was due to a failure in creating the VMBridge instance, and further investigation is needed to resolve the underlying problem.
    - Summary: App crashed due to an `IllegalStateException` when attempting to initialize the JavaScript engine, specifically related to the VMBridge instance creation failure.
    - [Resolved Issue#6](https://github.com/euptron/CodeOps-Studio/issues/6)
    - FCS-ID: `8c42fe20ec460f1f2bb87c206438081f`
    
* Fix: `TreeViewFragment` context attachment issue.
    - Summary: App crashed due to an `IllegalStateException` when attempting to access the fragment's context while it was not attached.
    - [Resolved Issue#5](https://github.com/euptron/CodeOps-Studio/issues/5)
    - FCS-ID: `5665f61e9b837932df3c90422eccb283`
    
* Fix: `ChangeLogFragment` null reference issue. `ChangeLogFragment` now correctly handles null `RecyclerView` references, preventing crashes when checking if logs are loaded.
    - Summary: App crashed due to a null `RecyclerView` reference being accessed in the `checkIfLoaded()` method.
    - [Resolved Issue#4](https://github.com/euptron/CodeOps-Studio/issues/4)
    - FCS-ID: `4dfd5aed0ff4905420e59f5b3b09ce90`
    
* Fix: `NullPointerException` when handling back press in `SettingsPane` due to null `Fragment`
    - Summary: App crashed because a null `Fragment` was passed to the `equals()` method in the `isPrimaryNavigation()` check.
    - [Resolved Issue#3](https://github.com/euptron/CodeOps-Studio/issues/3)
    - FCS-ID: `516de67b0dd6824a7aec7f8bd411974d`
    
* Fix: Crash when binding log data in the `LogAdapter` due to `NullPointerException`
    - Summary: App crashed when a null `CharSequence` was passed to the `SpannableStringBuilder.append()` method in `onBindViewHolder()`
    - [Resolved Issue#2](https://github.com/euptron/CodeOps-Studio/issues/2)
    - FCS-ID: `50dfb50c980fcffba644e7fa8debbafd`
    
* Fix: Crash when accessing recent projects in the `WelcomePane` class due to `NullPointerException`
    - Summary: App crashed as a result of a null file path being passed to the `File.exists` method 
    - [Resolved Issue#1](https://github.com/euptron/CodeOps-Studio/issues/1)
    - FCS-ID: `ba22d747cbd53061443dfc1c7a7698bf`

### Docs

* Improved [README](./README.md)
* Improved [CONTRIBUTING](./CONTRIBUTING.md)