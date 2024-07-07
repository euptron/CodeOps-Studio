#!/bin/bash

# Define the license header for non-XML files
LICENSE_HEADER_NON_XML="/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/etidoUP/CodeOps-Studio
 * Copyright (C) 2024 EUP
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
 * If you have more questions, feel free to message EUP if you have any
 * questions or need additional information. Email: etido.up@gmail.com
 *************************************************************************/
 
"

# Define the license header for XML files
LICENSE_HEADER_XML="<?xml version=\""1.0"\" encoding=\""utf-8"\"?>
<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ This file is part of CodeOps Studio.
 ~ CodeOps Studio - code anywhere anytime
 ~ https://github.com/etidoUP/CodeOps-Studio
 ~ Copyright (C) 2024 EUP
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
 ~ If you have more questions, feel free to message EUP if you have any
 ~ questions or need additional information. Email: etido.up@gmail.com
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->

"

# Function to add the license header to a file
add_license_header() {
  local file="$1"
  local manual_add="$2"

  if [[ "$manual_add" == "true" ]]; then
    # Add header to a specific file
    if [[ "$file" == *.xml ]]; then
      echo "$LICENSE_HEADER_XML$(cat "$file")" > "$file"
    else
      echo "$LICENSE_HEADER_NON_XML$(cat "$file")" > "$file"
    fi
  else
    # Add header to files found by find command
    if [[ "$file" == *.xml ]]; then
      if ! grep -q "This file is part of CodeOps Studio" "$file"; then
        echo "$LICENSE_HEADER_XML$(cat "$file")" > "$file"
      fi
    else
      if ! grep -q "This file is part of CodeOps Studio" "$file"; then
        echo "$LICENSE_HEADER_NON_XML$(cat "$file")" > "$file"
      fi
    fi
  fi
}

# Function to show progress
show_progress() {
  local file="$1"
  echo "Adding license header to: $file"
}

# Export the functions to be used with find
export -f add_license_header
export -f show_progress
export LICENSE_HEADER_NON_XML
export LICENSE_HEADER_XML

# Directory to search for files (current directory)
SEARCH_DIR=$(pwd)

# Check if manual mode is enabled
if [[ "$1" == "--manual" ]]; then
  # Check if second argument (file path) is provided
  if [[ -n "$2" ]]; then
    add_license_header "$2" "true"
    echo "License header added to $2."
  else
    echo "Please provide a file path."
    exit 1
  fi
else
  # Find and process files with specific extensions
  find "$SEARCH_DIR" -type f \( -name "*.java" -o -name "*.py" -o -name "*.cpp" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" -o -name "*.kt" \) -print0 |
  while IFS= read -r -d $'\0' file; do
    add_license_header "$file"
    show_progress "$file"
  done

  echo "License headers added successfully to all applicable files."
fi
