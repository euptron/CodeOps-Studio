# CodeOps Studio - Remove License Header Script

## Overview

To maintain a clean codebase or prepare files for redistribution, you may need to **remove
previously added GNU GPL v3 license headers** from the project’s source files.

The `remove_license_header.sh` script automates this process by scanning for the **exact license
header text** used in CodeOps Studio and removing it if found.

This script works for the same file types as the `add_license_header.sh` script:

* **Code files:** `.java`, `.py`, `.cpp`, `.gradle`, `.kt`, `.kts`
* **XML files:** `.xml`

> [!NOTE]
> This script will only remove headers that match exactly the defined license text in the script.
> Files in `build/` and `compile/` directories are skipped.

## Features

1. Supports **automatic bulk removal** and **manual file targeting**.
2. Skips `build/` and `compile/` directories to avoid build artifacts.
3. Prevents accidental content loss by only removing the **exact matching header**.
4. Works for multiple code and XML formats.

## Usage

### Setup

**Ensure the script has execute permissions:**

```bash
chmod +x ./docs/remove_license_header.sh
```

### Automatic Header Removal

To remove license headers from all matching files in your project directory:

```bash
$ bash ./docs/remove_license_header.sh
```

### Removing Header from a Specific File

To manually remove the license header from a specific file:

```bash
$ bash ./docs/remove_license_header.sh --manual path/to/your/file.java
```

Replace `path/to/your/file.java` with the path to the file you want to clean.

---

## Customization

* To change the header text being detected, edit the `LICENSE_HEADER_NON_XML` and
  `LICENSE_HEADER_XML` variables in the script.
* Add more file extensions in the `find` command if needed.

---

## Pros and Cons

### Pros

* **Time-saving Automation:** Removes headers quickly across many files.
* **Accuracy:** Matches the exact header to avoid removing unintended content.
* **Flexibility:** Supports bulk directory operations or single-file processing.

### Cons

* **Exact Match Required:** Will not remove headers with modifications unless updated in the script.
* **Permission Issues:** Requires correct execution permissions in Termux or your shell environment.

---

## Troubleshooting

| Issue                | Solution                                                   |
|----------------------|------------------------------------------------------------|
| `Permission denied`  | Run `chmod +x remove_license_header.sh`                    |
| No header removed    | Header in file may differ from script’s stored text        |
| Incorrect path error | Verify you are using a valid file path                     |
| Script skips files   | Ensure the extension is included in the `find` search list |

---

## Contact

For further assistance or questions regarding `remove_license_header.sh`, please contact me
at [euptron@gmail.com](mailto:euptron@gmail.com).