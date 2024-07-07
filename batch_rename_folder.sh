#!/bin/bash

# Function to replace text in a directory name
replace_text_in_dir() {
  local dir="$1"
  local search_text="$2"
  local replace_text="$3"

  # Get the new directory name
  local new_dir=$(echo "$dir" | sed "s/\b${search_text}\b/${replace_text}/g")

  # Check if the new directory name is different from the old one
  if [[ "$dir" != "$new_dir" ]]; then
    # Rename the directory
    mv "$dir" "$new_dir"
    echo "Renamed \"$dir\" to \"$new_dir\""
  else
    echo "No match for \"$search_text\" found in \"$dir\""
  fi
}

# Export the function to be used with find
export -f replace_text_in_dir

# Prompt the user for the text to be replaced and the replacement text
read -p "Enter the text to be replaced: " search_text
read -p "Enter the replacement text: " replace_text

# Directory to search for directories (current directory)
SEARCH_DIR=$(pwd)

# Find and process directories
find "$SEARCH_DIR" -type d -exec bash -c 'replace_text_in_dir "$0" '"$search_text"' '"$replace_text"'' {} \;

echo "Directory renaming process completed."
