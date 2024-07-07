#!/bin/bash

# Number of lines in the license header for non-XML files
LICENSE_HEADER_LINES_NON_XML=23

# Number of lines in the license header for XML files
LICENSE_HEADER_LINES_XML=24

# Function to remove the license header from a file
remove_license_header() {
  local file="$1"
  local manual_remove="$2"

  if [[ "$manual_remove" == "true" ]]; then
    # Remove header from a specific file
    if [[ "$file" == *.xml ]]; then
      tail -n +$(($LICENSE_HEADER_LINES_XML + 1)) "$file" > temp && mv temp "$file"
    else
      tail -n +$(($LICENSE_HEADER_LINES_NON_XML + 1)) "$file" > temp && mv temp "$file"
    fi
  else
    # Remove header from files found by find command
    if [[ "$file" == *.xml ]]; then
      if grep -q "This file is part of CodeOps Studio" "$file"; then
        tail -n +$(($LICENSE_HEADER_LINES_XML + 1)) "$file" > temp && mv temp "$file"
      fi
    else
      if grep -q "This file is part of CodeOps Studio" "$file"; then
        tail -n +$(($LICENSE_HEADER_LINES_NON_XML + 1)) "$file" > temp && mv temp "$file"
      fi
    fi
  fi
}

# Function to show progress
show_progress() {
  local file="$1"
  echo "Removing license header from: $file"
}

# Directory to search for files (current directory)
SEARCH_DIR=$(pwd)

# Export the function to be used with find
export -f remove_license_header
export -f show_progress
export LICENSE_HEADER_LINES_NON_XML
export LICENSE_HEADER_LINES_XML

# Check if manual mode is enabled
if [[ "$1" == "--manual" ]]; then
  # Check if second argument (file path) is provided
  if [[ -n "$2" ]]; then
    remove_license_header "$2" "true"
    echo "License header removed from $2."
  else
    echo "Please provide a file path."
    exit 1
  fi
else
  # Find and process files with specific extensions
  find "$SEARCH_DIR" -type f \( -name "*.java" -o -name "*.py" -o -name "*.cpp" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" -o -name "*.kt" \) -print0 |
  while IFS= read -r -d '' file; do
    remove_license_header "$file"
    show_progress "$file"
  done

  echo "License headers removed successfully from all applicable files."
fi
