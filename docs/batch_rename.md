# CodeOps Studio – Batch Text Replacement Script

## Overview

This script automates **case-sensitive, whole-word** text replacement across multiple files in a
project directory.
It allows you to choose the working directory, specify the text to search for, and provide the
replacement text.

Supported file types include:

* `.java`, `.py`, `.cpp`, `.gradle`, `.kt`, `.kts`, `.sh`
* `.xml`, `.md`

> \[!NOTE]
> The script processes only supported file types and can be customized to include others.

## Features

1. Prompts user for base directory (defaults to current working directory)
2. Uses **whole-word, case-sensitive** matching to avoid accidental replacements
3. Processes multiple file formats
4. Provides real-time feedback for each file updated or skipped

## Usage

### Setup

**Ensure the script has execute permissions:**

```bash
chmod +x ./docs/batch_rename.sh
```

### Automatic Replacement

To replace text in all supported files in the chosen directory:

```bash
$ bash ./docs/batch_rename.sh
```

You will be prompted for:

1. Base directory (press Enter to use current directory)
2. Text to be replaced
3. Replacement text

---

### Example

```bash
$ bash ./docs/batch_rename.sh
Enter the base directory (press Enter to use current directory): /home/user/project
Enter the text to be replaced: oldValue
Enter the replacement text: newValue
Replacing "oldValue" with "newValue" in MainActivity.java
No match for "oldValue" found in settings.gradle
Text replacement process completed.
```

### Customization

* Add or remove supported file extensions inside the `find` command in the script.

## Pros and Cons

### Pros

* **Fast Automation:** Handles replacements across large projects efficiently.
* **Accuracy:** Whole-word matching prevents partial replacements.
* **Flexible:** Easy to add more file types.

### Cons

* **Basic Shell Skills Required:** For modifications or troubleshooting.
* **File Type Limit:** Must be updated manually to handle new formats.

## Troubleshooting

| Issue                | Solution                                          |
|----------------------|---------------------------------------------------|
| `Permission denied`  | Run `chmod +x batch_rename.sh`              |
| No matches found     | Ensure the text matches exactly (case-sensitive). |
| Incorrect path error | Provide a valid base directory path.              |

## Contact

For further assistance or questions regarding `batch_rename.sh`, please contact me
at [euptron@gmail.com](mailto:euptron@gmail.com).