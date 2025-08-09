#!/bin/bash

# Ask for directory first
read -rp "Enter the base directory (press Enter to use current directory): " USER_INPUT_DIR
  SEARCH_DIR="${USER_INPUT_DIR:-$(pwd)}"

  if [[ ! -d "$SEARCH_DIR" ]]; then
    echo "Error: '$SEARCH_DIR' is not a valid directory."
    exit 1
  fi

# Function to replace text in a directory name
replace_text_in_dir() {
  local dir="$1"
  local search_text="$2"
  local replace_text="$3"

  # Get the new directory name
  local new_dir
  new_dir="${dir//${search_text}/${replace_text}}"

  if [[ "$dir" != "$new_dir" ]]; then
    mv "$dir" "$new_dir"
    echo "Renamed \"$dir\" to \"$new_dir\""
  else
    echo "No match for \"$search_text\" found in \"$dir\""
  fi
}

export -f replace_text_in_dir

read -rp "Enter the text to be replaced: " search_text
read -rp "Enter the replacement text: " replace_text

# Find and process directories
find "$SEARCH_DIR" -type d -exec bash -c 'replace_text_in_dir "$0" '"$search_text"' '"$replace_text"'' {} \;

echo "Directory renaming process completed."
