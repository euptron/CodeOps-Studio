#!/bin/bash

# Function to replace text in a file
replace_text() {
  local file="$1"
  local search_text="$2"
  local replace_text="$3"

  # Use grep to check if the search text exists in the file
  if grep -qw "$search_text" "$file"; then
    # Perform the replacement using sed
    sed -i "s/\b${search_text}\b/${replace_text}/g" "$file"
    echo "Replacing \"$search_text\" with \"$replace_text\" in $file"
  else
    echo "No match for \"$search_text\" found in $file"
  fi
}

# Export the function to be used with find
export -f replace_text

# Prompt the user for the text to be replaced and the replacement text
read -p "Enter the text to be replaced: " search_text
read -p "Enter the replacement text: " replace_text

# Directory to search for files (current directory)
SEARCH_DIR=$(pwd)

# Find and process files with specific extensions
find "$SEARCH_DIR" -type f \( -name "*.java" -o -name "*.py" -o -name "*.cpp" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" -o -name "*.kt" -o -name "*.sh" \) -exec bash -c 'replace_text "$0" '"$search_text"' '"$replace_text"'' {} \;

echo "Text replacement process completed."
