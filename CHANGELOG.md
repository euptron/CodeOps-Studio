Change Log
==========

All notable changes to this project will be documented in this file, this project
uses [semantic versioning `2.0.0` spec](https://semver.org/spec/v2.0.0.html) in the format
`major.minor.patch(-prerelease)(+buildmetadata)`.

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

Release 1.1.0 beta
----------------------
_21-12-2025_(Sunday, December 21, 2025)

### Added

* Versioning Manager to enforce our Custom SemVer
* Onboarding fragment – the first thing a user sees must be nice 🙂 

### Changes

* Extensive internal refactoring and restructuring of the codebase

### Improvements

* Optimized push notifications with targeting
    - Summary: Push notifications are now more efficient and can be targeted more accurately
      to improve delivery and user engagement.
* Jump to line dialog to use the Command-Palette
* Enforced EdgeToEdge across all supported api versions
* Pane system
* Overall application stability
* Performance and memory usage
* Foundation laid for future features and systems
* LiveServer to server both files and directories
* Internet connectivity check via the `IPProvider.java`

### Fixed

* Numerous undocumented bug fixes
    - Summary: A significant number of issues were resolved during development,
      most of which were not individually logged due to their volume.
      
* Fix: [Opened Issues](https://github.com/euptron/CodeOps-Studio/issues/12)
* Fix: [Opened Issues](https://github.com/euptron/CodeOps-Studio/issues/7)
* Fix: [Opened Issues](https://github.com/euptron/CodeOps-Studio/issues/6)

### Security

* Secured CodeOps Studio private and sensitive data
    - Summary: Strengthened encryption and protection of sensitive assets across the build,
      deployment, and runtime pipeline. This includes safeguarding secrets used in GitHub Actions,
      Firebase services, code signing keys, and internal application data to ensure user data
      remains private and protected.

### Docs

* updated [README](./README.md)
* updated [CONTRIBUTING](./CONTRIBUTING.md)
* Introduced custom versioning system
    - Summary: Implemented a variation of semantic versioning with specific rules for release
      tracking and consistency across the project.

Release 1.0.5 beta
----------------------
_(Date unavailable)_

### Added
* introduced `PaneWindow` to handle the panes in a decoupled fashion
* In-app update system and update handling logic
* Theme export utility
    - Allows exporting the currently active application theme to an XML file
* Tap detection and measurement system
* More `CodeEditorPane` file functions e.g save-as, reload-file, reload-file with-charset, file statistics, next and previous cursor location movement and lite mode
* `ILog.java` to expose the logs sent by Log.java to enhance debugging.
* GM3 Color theme exporter to export app theme at runtime
* Command-Palette to enhance quick actions and workflows
* Debug overlay
    - Activated by long-pressing the top layout for ~3–4 seconds and releasing

### Changes

* Major codebase revamp in preparation for future releases

### Improvements

* Migrated all pane functionality to `PaneWindow.java`
* Storage manager stability and reliability
* Memory management across the application
* Overall application stability and performance

### Fixed

* Countless bugs and memory leaks
    - Summary: A very large number of bugs were fixed during this release cycle.
      Many of them were not tracked individually due to volume and time constraints.

### Removed

* `PaneUtil.java` in favor of `PaneWindow.java`
* Template manager
    - Reason: Will be replaced with a more robust, offline-first template system
      intended to support a future community-driven workflow.

Release 1.0.4 beta
----------------------
_09-03-2025_(Sunday, Mar 9, 2025)

> Unfortunately I am unable to publish **1.1.0** because I lost the source code which is painful.
> The goal currently is to make sure this project works properly across the supported Android APIs
> Until I can recover the lost codes I would not implement certain feats, fixes and improvements

### Added

* ????

### Changes

* Crashlytics and Analytics are now automatically sent, user can opt out in the setting
* Theme system updated to properly support **Dynamic Colors**

### Improvements

* Tweaked `BinaryFileChecker` to be more sensitive to binary file detection

### Fixed

* Fix: Undo / Redo synchronization issue
    - Summary: Redo operations could not be performed instantly due to editor state
      desynchronization. This has been fixed to ensure immediate and consistent redo behavior.

* A large number of additional bug fixes were made but not fully documented.

### Removed

* The `ast-core` library until further notice.

Release 1.0.3 beta
----------------------
_09-03-2025_(Sunday, Mar 9, 2025)

### Added

* Buffer size selection for dynamic IO operations
* Facebook page link to `Socials`
* Projects created from templates are now added to the recent project list
* File system monitoring

### Changes

* Moved some pane window functionality from `BaseFragment.java` to `PaneUtil.java`
* Removed support for editing removable storage volume:
    - I'm working on a dual state file system API `MetaDocument.java` that uses both `File` and
      `Document` `API`'
* Updated inline copyright within all supported file type, ps: it's a new year
* Disable auto-rotate until i figure out how to stop code editor pane duplication

### Improvements

* Optimised `BinaryFileChecker.java` to use weight instead of a majority rule system
* Optimised `CodeEditor Pane` to use resources optimally.
* Optimised `BaseFragment.java` to handle resources optimally.
* File rename logic
* Code editor settings `Pin line number` sync; in terms which works
* `Relative close depth` sync; New configuration is synced in realtime with the **Code Editor** so
  you don't restart the app to apply the changes.
* `Auto closing brackets` sync; New configuration is synced in realtime to the **Code Editor** so
  you don't restart the app to apply the changes.
* `Close unpinned project panes` sync; New configuration is synced in realtime so you don't restart
  the app to apply the changes.
* Generally i've made a lot of improvements i still failed to track.
* Release notes support either plane text or html text.

### Fixed

* Fix: [Opened Issues](https://github.com/euptron/CodeOps-Studio/issues)

* Fix: False positives within the `BinaryFileChecker`

* Fix:  Crash on android devices 11 and above due to storage related issues
    - Summary: Crash occurs when user tries to perform file/folder selection

* Fix:  Crash on that occurs at certain times when user tries perform the `Clone git repository`
  action
    - Summary: This error occurred because the `Layout binding` was initialized later i.e after the
      **VM** observer at `GitRepository.java:123`

* Fix: Failure to activate Code Editor `pin line number` preference
    - Summary: Logic for this preference to be activated was not implemented

* Fix: Sync error that occurred when refreshing the **Code Editor** color scheme
    - Summary: If the `Auto complete window` preference is disabled when ever the editor color
      scheme is refreshed the autocomplete window is displayed.

* Fix: Failure to enable or disable `Font ligatures` when the preference is selected
    - Summary: Logic to sync this feature with the **Code Editor** was not implemented

* A lot of bug fixes were made, i only added those which were worth taking note of.

### Removed

* Opening and editing files from a removable storage

Release 1.0.2 beta
----------------------
_26-12-2024_(Thursday, Dec 26, 2024)

### Added

* Extra symbols for the input panel
* The input action MCR, MCL, MCU, MCD are essentially cursor actions where M (Move), C (Cursor), L(
  Left), R(Right), U(Up), D(Down)

### Change

* Moved Uri to file conversion from FileUtil to independent class `FileUriMediator.java`
* Decoupled storage permission handling to `MainActiviy.java` from `MainFragment.java`
* Slowly migrating code base to more structural format

### Improvements

* Storage permission management
* Optimised the `ContextLifeCycleObserver.java` class to handle URI related task gracefully
* Optimised `LiveServer.java` to load files optimally.
* Optimised case conversion for `ContextualCodeEditor.java`
* Generally i've made a lot of improvements i failed to track.

### Fixed

* Fix: Workaround for loading files via `LiveServer` in the `WebViewPane.java` to an actual solution
    - Summary: Prior to this release when you init the `LiveServer` there is Ui lag due to the
      blocking operation, workaround was to load portion of the result and wait for the UI to
      settled before loading the blocking code.

* Fix: Fixed the horrible crash that occurs when you select a file or folder
    - Summary: Prior to this release crashes were a lot to often, i found that it mostly occurs on
      device with higher APIS

* Crash when selecting a directory on android devices 11 and above
* `ArrayOutOfBoundException` that occurs when we split the document id on certain dirs like (
  emulated root(primary) & document(home)
* Failure to request storage permission on Android devices 11 and above

Release 1.0.1 beta
----------------------

_13-09-2024_ (Thursday, Sep 13, 2024)

### Added

* Option to refresh file tree
* AST core module for future Node management
* Support for `.apk` file installation
* Progress listener for case conversion in the `ContextualCodeEditor`
* Support for more symbols auto completions `\`„ “ « ‚ ‘ ‹\`

### Changes

* Disable code obfuscation and resource shrinking `minifyEnabled` and `shrinkResources` `= false`
  because it breaks CodeOps Studio.

### Improvements

* Optimized file tree loading performance and reduced memory usage
* JavaDoc improvements
* Refactored Code base
* `Pane` class ID generation: shared ID list for all instances of the `Pane` class
* `Pane` class ID generation method: `Pane#generateUUID()`
* Optimized `Wizard#getDeviceCountry(Context)` method to retrieve the name of **Country** a device
  is currently in. Crashlytics now collects the actual **Country Name** instead of the
  `Device Locale`
* Revamp detection of invalid last opened project before opening
* Optimized `IdeApplication` and `CrashActivity` for handling crash intent
* Revamped `TreeView-TreeNode` children sorting comparator
* Optimised `ContextualCodeEditor#toLowerCase(String)` and
  `ContextualCodeEditor#toUpperCase(String)` method by reducing time complexity from O(n^2) to O(n)
  that was caused by the `string = string + ...` reallocation
* Case conversion by the `ContextualCodeEditor` is now possible even when `ClipBoard` limit is
  exceeded
* Optimized line text duplication by the `CodeEditor`
* Optimized binary file detection, before a file is flagged as binary we compare how many times it
  has been flagged as a binary across multiple `check+points` against how many times it didn't,
  making the whole process more effective.

### Fixed

* Fix: Case conversion failure.
    - Summary: Case conversion failed because the result from the `AsyncTask#runNonCancelable` was
      not parsed into the editor.

* Fix: Crash when parsing a string into `ReleaseType(String)` in ChangelogAdapter due to
  `NullPointerException`
    - Summary: App crashed when trying to load saved release notes due on a null object
      `ReleaseType(null)` reference in ChangelogAdapter.

* Fix: Pane Tab duplication. Proguard actually broke the project so it's disabled until further
  notice.

* Fix: `CodeEditorPane` persistency. `CodeEditorPane` now loads persisted arguments and restores
  them accordingly without text content history

* Fix: Failure to properly detect invalid projects when loading the last opened project.
    - Summary: App crashed as a result of a null file path being passed to the `File.exists` method

* Fix: Crash due to null `Log` object in `Log.equals()` method*
    - Summary: The app crashes with a `NullPointerException` when the `equals()` method is invoked
      on a null `Log` object in the `Log.equals()` method. This issue arises during item comparisons
      in the `RecyclerView`, particularly when updating the list using `AsyncListDiffer` in the
      `LogAdapter`. The crash often occurs when selecting an unsupported `Uri`.
    - [Resolved Issue#10](https://github.com/euptron/CodeOps-Studio/issues/10)
    - FCS-ID: `33308dfde6721aa4e51f9010a1f307ea`

* Fix: Crash when accessing a null `CircularProgressIndicator` in `CodeEditorPane.setLoading()`
    - Summary: The app crashed due to a `NullPointerException` when attempting to access a null
      `CircularProgressIndicator` in the `CodeEditorPane.setLoading()` method. This occurred while
      loading a file, where the `progressbar` was not properly initialized.
    - [Resolved Issue#9](https://github.com/euptron/CodeOps-Studio/issues/9)
    - FCS-ID: `fcc3d4f7d3825fd148be619cf777948b`

* Fix: Crash when accessing null `ContextualCodeEditor` in `CodeEditorPane.canUndo()`
    - Summary: The app crashed due to a `NullPointerException` when attempting to access a null
      `ContextualCodeEditor` object in the `CodeEditorPane.canUndo()` method. This occurred during
      the preparation of the toolbar options menu when the undo state was being checked.
    - [Resolved Issue#8](https://github.com/euptron/CodeOps-Studio/issues/8)
    - FCS-ID: `c6f4d617cc63acaa3fe262dbb1e948ca`

* Fix: Crash when accessing a null `ViewHolder` in `RecyclerView`
    - Summary: App crashed due to a `NullPointerException` when trying to reference a null
      `ViewHolder` in the `RecyclerView`.
    - [Resolved Issue#7](https://github.com/euptron/CodeOps-Studio/issues/7)
    - FCS-ID: `c12fd425b630739a67de5d35ea207be5`

* Fix: JavaScript engine initialization failure. The issue was due to a failure in creating the
  VMBridge instance, and further investigation is needed to resolve the underlying problem.
    - Summary: App crashed due to an `IllegalStateException` when attempting to initialize the
      JavaScript engine, specifically related to the VMBridge instance creation failure.
    - [Resolved Issue#6](https://github.com/euptron/CodeOps-Studio/issues/6)
    - FCS-ID: `8c42fe20ec460f1f2bb87c206438081f`

* Fix: `TreeViewFragment` context attachment issue.
    - Summary: App crashed due to an `IllegalStateException` when attempting to access the
      fragment's context while it was not attached.
    - [Resolved Issue#5](https://github.com/euptron/CodeOps-Studio/issues/5)
    - FCS-ID: `5665f61e9b837932df3c90422eccb283`

* Fix: `ChangeLogFragment` null reference issue. `ChangeLogFragment` now correctly handles null
  `RecyclerView` references, preventing crashes when checking if logs are loaded.
    - Summary: App crashed due to a null `RecyclerView` reference being accessed in the
      `checkIfLoaded()` method.
    - [Resolved Issue#4](https://github.com/euptron/CodeOps-Studio/issues/4)
    - FCS-ID: `4dfd5aed0ff4905420e59f5b3b09ce90`

* Fix: `NullPointerException` when handling back press in `SettingsPane` due to null `Fragment`
    - Summary: App crashed because a null `Fragment` was passed to the `equals()` method in the
      `isPrimaryNavigation()` check.
    - [Resolved Issue#3](https://github.com/euptron/CodeOps-Studio/issues/3)
    - FCS-ID: `516de67b0dd6824a7aec7f8bd411974d`

* Fix: Crash when binding log data in the `LogAdapter` due to `NullPointerException`
    - Summary: App crashed when a null `CharSequence` was passed to the
      `SpannableStringBuilder.append()` method in `onBindViewHolder()`
    - [Resolved Issue#2](https://github.com/euptron/CodeOps-Studio/issues/2)
    - FCS-ID: `50dfb50c980fcffba644e7fa8debbafd`

* Fix: Crash when accessing recent projects in the `WelcomePane` class due to `NullPointerException`
    - Summary: App crashed as a result of a null file path being passed to the `File.exists` method
    - [Resolved Issue#1](https://github.com/euptron/CodeOps-Studio/issues/1)
    - FCS-ID: `ba22d747cbd53061443dfc1c7a7698bf`

### Docs

* Improved [README](./README.md)
* Improved [CONTRIBUTING](./CONTRIBUTING.md)
