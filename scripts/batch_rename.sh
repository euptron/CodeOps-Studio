#!/bin/bash

# Ask for directory first
read -rp "Enter the base directory (press Enter to use current directory): " USER_INPUT_DIR
SEARCH_DIR="${USER_INPUT_DIR:-$(pwd)}"

  if [[ ! -d "$SEARCH_DIR" ]]; then
    echo "Error: '$SEARCH_DIR' is not a valid directory."
    exit 1
  fi

# Function to replace text in a file
replace_text() {
  local file="$1"
  local search_text="$2"
  local replace_text="$3"

  if grep -qw "$search_text" "$file"; then
    sed -i "s/\b${search_text}\b/${replace_text}/g" "$file"
    echo "Replacing \"$search_text\" with \"$replace_text\" in $file"
  else
    echo "No match for \"$search_text\" found in $file"
    fi
}

export -f replace_text

read -rp "Enter the text to be replaced: " search_text
read -rp "Enter the replacement text: " replace_text

# Find and process files with specific extensions
find "$SEARCH_DIR" -type f \( -name "*.java" -o -name "*.py" -o -name "*.cpp" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" -o -name "*.kt" -o -name "*.sh" \) -exec bash -c 'replace_text "$0" '"$search_text"' '"$replace_text"'' {} \;

echo "Text replacement process completed."
