# CodeOps Studio - Batch Text Replacement Script

## Overview

The `batch_text_replace.sh` script automates the process of replacing text within files in your project directory using match case and whole word formula. It allows you to specify the text to be replaced and the replacement text, then processes all files within the directory to make the changes.

## Features

- Replaces specified text in multiple file types.
- Provides real-time feedback for each file processed.
- Supports batch processing of files in the current directory.
- Customizable to target specific file types.

## Usage

### Setup

1. **Navigate to Your Project Directory:**
   - Open Termux and navigate to the directory containing `batch_text_replace.sh`:
     ```sh
     cd /path/to/your/project
     ```

2. **Set Execution Permissions:**
   - Ensure the script has execute permissions:
     ```sh
     chmod +x batch_rename.sh
     ```

### Running the Script

To replace text in all files in your project directory:

```sh
$ bash batch_rename.sh
```

### Script Workflow

1. **Prompt for Search and Replace Text:**
   - The script will ask you to enter the text to be replaced and the replacement text.

2. **Text Replacement Process:**
   - The script searches for files with specific extensions and replaces the specified text.

### Supported File Types

- `.java`
- `.py`
- `.cpp`
- `.xml`
- `.gradle`
- `.kts`
- `.kt`
- `.sh`

### Example Usage

1. **Start the Script:**
   ```sh
   $ bash batch_rename.sh
   ```

2. **Enter the Text to Be Replaced:**
   ```
   Enter the text to be replaced: oldText
   ```

3. **Enter the Replacement Text:**
   ```
   Enter the replacement text: newText
   ```

4. **Processing Files:**
   - The script will process each file and provide feedback, such as:
     ```
     Replacing "oldText" with "newText" in file1.java
     No match for "oldText" found in file2.xml
     ```

5. **Completion:**
   ```
   Text replacement process completed.
   ```

## Pros and Cons

### Pros

- **Automated Process:** Efficiently replaces text across multiple files, saving time and effort.
- **Real-time Feedback:** Provides immediate feedback on the text replacement process for each file.
- **Customizable:** Can be modified to include or exclude specific file types.

### Cons

- **Skill Requirement:** Basic knowledge of shell scripting is necessary for customization and troubleshooting.
- **Permission Issues:** Ensure correct permissions (`chmod +x`) to execute the script in Termux.

## Troubleshooting

- **Permission Denied:** Grant execute permissions (`chmod +x batch_text_replace.sh`) if needed.
- **Search/Replace Issues:** Ensure the text to be replaced and the replacement text are correctly entered.

## Contact

For further assistance or questions regarding `batch_text_replace.sh`, please contact:

- **EUP** (etido.up@gmail.com)