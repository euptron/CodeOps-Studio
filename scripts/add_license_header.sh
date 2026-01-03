#!/bin/bash

#
# This file is part of CodeOps Studio.
# CodeOps Studio - Code anywhere anytime
# https://github.com/euptron/CodeOps-Studio
# Copyright (C) 2024-2026 Etido Peter
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/
#
# If you have more questions, feel free to message Etido Peter if you have any
# questions or need additional information. Email: euptron@gmail.com
#

# Define the license header for non-XML files
LICENSE_HEADER_NON_XML="/*
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

"

# Define the license header for XML files
LICENSE_HEADER_XML="
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

"

add_license_header() {
  local file="$1"
  local manual_add="$2"

  if [[ "$manual_add" == "true" ]]; then
    if [[ "$file" == *.xml ]]; then
      echo "$LICENSE_HEADER_XML$(cat "$file")" > "$file"
    else
      echo "$LICENSE_HEADER_NON_XML$(cat "$file")" > "$file"
    fi
  else
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

show_progress() {
  local file="$1"
  echo "Adding license header to: $file"
}

export -f add_license_header
export -f show_progress
export LICENSE_HEADER_NON_XML
export LICENSE_HEADER_XML

if [[ "$1" == "--manual" ]]; then
  if [[ -n "$2" ]]; then
    add_license_header "$2" "true"
    echo "License header added to $2."
  else
    echo "Please provide a file path."
    exit 1
  fi
else
  read -rp "Enter the base directory (press Enter to use current directory): " USER_INPUT_DIR
  SEARCH_DIR="${USER_INPUT_DIR:-$(pwd)}"

  if [[ ! -d "$SEARCH_DIR" ]]; then
    echo "Error: '$SEARCH_DIR' is not a valid directory."
    exit 1
  fi

  echo "Scanning directory: $SEARCH_DIR"

  # Find and process files with specific extensions, excluding 'build' and 'compile' directories
  find "$SEARCH_DIR" -type d \( -iname "build" -o -iname "compile" \) -prune -o \
  -type f \( -name "*.java" -o -name "*.py" -o -name "*.cpp" -o -name "*.xml" -o -name "*.gradle" -o -name "*.kts" -o -name "*.kt" \) -print0 |
  while IFS= read -r -d $'\0' file; do
    add_license_header "$file"
    show_progress "$file"
  done

  echo "License headers added successfully to all applicable files."
fi
