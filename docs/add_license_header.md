# CodeOps Studio - Add License Header Script

## Overview

To ensure consistent licensing and attribution across the **CodeOps Studio OSS** repository, **all
source files** in the
project must include the appropriate **GNU GPL v3** license header as well as the author tag.

This script automates the addition of license headers for various file types including:

* **Code files:** `.java`, `.py`, `.cpp`, `.gradle`, `.kt`, `.kts`
* **XML files:** `.xml`

> [!NOTE]
> This script avoids adding headers to files within `build/` and `compile/` directories.

### For Code Files

Applies to: `.java`, `.py`, `.cpp`, `.gradle`, `.kt`, `.kts`

```java
/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: euptron@gmail.com
 */
 ```

### For XML Files

Applies to: `.xml`

```xml
<!--
  ~ This file is part of CodeOps Studio.
  ~ CodeOps Studio - Code anywhere anytime
  ~ https://github.com/euptron/CodeOps-Studio
  ~ Copyright (C) 2024-2026 Etido Peter
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program. If not, see https://www.gnu.org/licenses/
  ~
  ~ If you have more questions, feel free to message Etido Peter if you have any
  ~ questions or need additional information. Email: euptron@gmail.com
  -->
<root>
    <item>Some Value 1</item>
    <item>Some Value 2</item>
</root>
```

## Features

1. Supports both **automatic bulk updates** and **manual file targeting**
2. Skips `build/` and `compile/` directories
3. Prompts user for base directory (or defaults to current working directory)
4. Prevents duplicate headers by checking file content
5. Supports multiple code formats

## Usage

### Setup

**Ensure the script has execute permissions:**

```bash
chmod +x ./docs/add_license_header.sh
```

### Automatic Header Addition

To add license headers to all files in your project directory:

```bash
$ bash ./docs/add_license_header.sh
```

### Adding Header to a Specific File

Run the below to manually add a license header to a specific file:

```bash
$ bash ./docs/add_license_header.sh --manual path/to/your/file.java
```

Replace `path/to/your/file.java` with the actual path to the file you want to modify.

---

### Customization

* To customize the license content, edit the `LICENSE_HEADER_NON_XML` and `LICENSE_HEADER_XML`
  variables inside the script.
* You can add more file extensions in the `find` command section if needed.

## Pros and Cons

### Pros

- **Time-saving Automation:** Automates the task of adding license headers, reducing manual effort.
- **Compliance Assurance:** Ensures adherence to GNU General Public License (GPL) and
  project-specific licensing terms.
- **Flexibility:** Handles bulk updates or specific file modifications seamlessly.

### Cons

- **Skill Requirement:** Basic knowledge of shell scripting is necessary for customization and
  troubleshooting.
- **Permission Issues:** Ensure correct permissions (`chmod +x`) to execute the script in Termux.

## Troubleshooting

| Issue                  | Solution                                               |
|------------------------|--------------------------------------------------------|
| `Permission denied`    | Run `chmod +x add_license_header.sh`                   |
| No header added        | File may already contain the header                    |
| Incorrect path error   | Ensure you provide a valid file path                   |
| Not scanning directory | Check that the provided base directory actually exists |

## Contact

For further assistance or questions regarding `add_license_header.sh`, please contact me
at [euptron@gmail.com](mailto:euptron@gmail.com).