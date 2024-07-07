/*************************************************************************
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
 
   package com.eup.codeopsstudio.util;

import com.eup.codeopsstudio.common.util.FileUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;

/**
 * Utility class to determine whether a file contains binary data.
 *
 * <p>Binary files, unlike text files, contain data encoded in binary format, commonly used for
 * storing images, multimedia, or executable content. This class employs multiple strategies,
 * including magic numbers, file extensions, content entropy, and MIME types, to accurately classify
 * files.
 *
 * <p>For more information on binary files, refer to <a
 * href="https://en.wikipedia.org/wiki/Binary_file">Binary File</a>.
 *
 * <ul>
 *   TODO: Create plugin to update file magic numbers and binary file extensions like use json
 *
 * @author EUP
 */
public class BinaryFileChecker {

  private static final int MAX_READ_BYTES = 256; // higher tweaks may affect performance
  private static final int BYTE_MASK = 0xFF;
  private static final byte LOWER_BOUND = 0x09;
  private static final byte UPPER_BOUND = 0x20;
  private static final byte BACKSPACE = 0x08;
  private static final byte LOWER_BOUND_EXCLUSIVE = 0x0E;
  private static final double ENTROPY_THRESHOLD = 7;
  private static final int MAX_SAMPLE_SIZE = 1024;

  private BinaryFileChecker() {
    // HC
  }

  public static boolean isBinaryFile(File file) throws IOException {
    return isBinaryFile(file, false);
  }

  public static boolean isBinaryFile(File file, boolean useFileFormat) throws IOException {

    if (file.isDirectory()) {
      throw new IllegalArgumentException("Provided file is a directory!");
    }

    // Check for magic numbers in header
    try (InputStream inputStream = new FileInputStream(file)) {
      byte[] fileContentBuffer = new byte[MAX_READ_BYTES];
      int bytesRead = inputStream.read(fileContentBuffer, 0, MAX_READ_BYTES);

      if (bytesRead < 0) {
        // Possibly an empty file!
        return false;
      }

      if (checkForMagicNumbers(fileContentBuffer)) {
        return true;
      }

      if (hasHighEntropy(fileContentBuffer)) {
        return true;
      }
      
      /* 
       * Not so accurate...flags numbers as binary.
       * TODO: Improve algorithm to know determine if number sequence is that of binary rather than flagging
       * non-ascii chars as binary
       */
      if (containsBinaryContent(fileContentBuffer)) {
        return true;
      }
      
      if (useFileFormat) {
        if (isBinaryExtension(file)) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean checkForMagicNumbers(byte[] buffer) {
    for (String magicNumber : FILE_MAGIC_NUMBERS) {
      if (containsMagicNumber(buffer, magicNumber)) {
        return true;
      }
    }
    return false;
  }

  private static boolean containsMagicNumber(byte[] buffer, String magicNumber) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : buffer) {
      String hex = Integer.toHexString(b & BYTE_MASK);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    String contentHex = hexString.toString().toUpperCase();
    if (contentHex.contains(magicNumber)) {
      return true;
    }
    return false; // potential flat binary
  }

  private static boolean containsBinaryContent(byte[] content) {
    // Check if the byte is within the ASCII printable character range
    for (byte b : content) {
      if ((b < LOWER_BOUND && b != BACKSPACE) || (b > LOWER_BOUND_EXCLUSIVE && b < UPPER_BOUND)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasHighEntropy(byte[] buffer) {
    int totalBytes = buffer.length;

    // Calculate the sample size as one-third of the total file size
    int sampleSize = Math.min(totalBytes / 3, MAX_SAMPLE_SIZE);

    // Ensure that the sample size is at least 1 byte
    sampleSize = Math.max(sampleSize, 1);

    int[] occurrences = new int[256]; // Assuming bytes are unsigned

    // Count occurrences of each byte in the sample
    for (int i = 0; i < sampleSize; i++) {
      occurrences[buffer[i] & BYTE_MASK]++;
    }

    // Calculate entropy based on the sampled data
    double entropy = 0;
    for (int count : occurrences) {
      if (count > 0) {
        double probability = (double) count / sampleSize;
        entropy -= probability * Math.log(probability) / Math.log(2);
      }
    }

    // If entropy exceeds threshold, return true
    if (entropy > ENTROPY_THRESHOLD) {
      return true;
    }

    // If entropy is below threshold, continue checking the rest of the buffer
    for (int i = sampleSize; i < totalBytes; i++) {
      occurrences[buffer[i] & BYTE_MASK]++;
      entropy = 0;
      for (int count : occurrences) {
        if (count > 0) {
          double probability = (double) count / (i + 1); // Incremental probability calculation
          entropy -= probability * Math.log(probability) / Math.log(2);
        }
      }
      if (entropy > ENTROPY_THRESHOLD) {
        return true;
      }
    }

    // If no high entropy found, return false
    return false;
  }

  private static boolean isBinaryExtension(File file) {
    String extension = FileUtil.getFileExtension(file);
    if (!Wizard.isEmpty(extension)) {
      for (String binaryExt : BINARY_FILE_FORMATS) {
        if (extension.equalsIgnoreCase(binaryExt)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * File magic numbers are the first bits (HEX BASED) of a file which is used as a unique refrence
   * when identifying the type of file.
   *
   * <p>NOTE: Using the file magic number we can easily detect the file type because we do not have
   * to search a complex file structure in other to identify the file type. However plain text files
   * have no magic number although they may contain a byte order mark.
   *
   * @see
   */
  private static final String[] FILE_MAGIC_NUMBERS = {
    // IMAGE FILES
    "FF D8 FF E0", // .jpg
    "89 50 4E 47", // .png
    "52 49 46 46", // .webp
    "47 49 46 38 39 61", // .gif (GIF89a)
    "47 49 46 38 37 61", // .gif (GIF87a)
    "42 4D", // .bmp
    "53 49 4D 50 4C 45", // .fits
    "47 4B 53 4D", // .gks
    "01 DA", // .rgb
    "F1 00 40 BB", // .itc
    "49 49 4E 31", // .nif
    "56 49 45 57", // .pm
    "25 21", // .[e]ps
    "59 A6 6A 95", // .ras
    "XX XX XX", // .tga
    "4D 4D 00 2A", // .tif
    "49 49 2A 00", // .tif
    "67 69 6D 70 20 78 63 66 20 76", // .xcf
    "23 46 49 47", // .fig
    "2F 2A 20 58 50 4D 20 2A 2F", // .xpm
    // VIDEO FILES
    "00 00 00 1C", // .mp3
    "1A 45 DF A3", // .mkv
    // AUDIO FILES
    "23 21 41 4D", // .amr
    "FF FB E4 64", // .mp3
    // Compressed files
    "1F 8B", // .gz
    "1F 8B 08", // .gzip
    "1F 9B", // .Z
    "45 5A", // .bz
    "42 5A", // .bzip2
    "42 5A 68", // .bzip2
    "50 4B 03 04", // .zip, .apk(s), .jar
    "50 4B 05 06", // .zip
    "50 4B 07 08", // .zip
    // Archive files
    "37 7A BC AF", // .7z
    "75 73 74 61", // .tar
    "XX XX", // .tar (pre-POSIX)
    "75 73 74 61 72", // .tar (POSIX)
    // COMPILED FILES
    "64 65 78 0A", // .dex
    "03 00 08 00", // .xml (Compiled)
    "CA FE BA BE", // bin
    "00 00 00 3A", // bin
    // OTHER FILES
    "77 4F 46 46", // .woff
    "00 01 00 00", // .ttf
    "77 4F 46 32", // .woff2
    "9F 4E 00 00", // .eot
    "4F 54 54 4F", // .otf
    "7F 45 4C 46", // .so
    "30 82 0A A8", // .keystore
    "25 50 44 46", // .pdf
    "CA FE BA BE", // Compiled Java class files (bytecode) and Mach-O binaries
    "CAFED00D", // CAFEBABE. When compressed with Pack200 the bytes
    "43 57 53 01", // .swf (Flash)
    "38 42 50 53", //  .8bps(Photoshop)
    "7F 45 4C 46", // .elf (Linux Executable)
    "4D 5A", // DOS/Windows executable
    "53 74 75 66", // StuffIt compressed data
    "1F 9D 90", // LZH compressed data
    "04 22 4D 18", // LHA compressed data
    "1A 45 DF A3", // Matroska video
    "D0 CF 11 E0", // Microsoft Office files (DOC/XLS/PPT)
    "0A 05 01 01", // Microsoft Office files (DOC/XLS/PPT)
  };

  private static final String[] BINARY_FILE_FORMATS = {
    "3dm",
    "3ds",
    "3g2",
    "3gp",
    "7z",
    "a",
    "aac",
    "adp",
    "afdesign",
    "afphoto",
    "afpub",
    "ai",
    "aif",
    "aiff",
    "alz",
    "ape",
    "apk",
    "appimage",
    "ar",
    "arj",
    "asf",
    "au",
    "avi",
    "bak",
    "baml",
    "bh",
    "bin",
    "bk",
    "bmp",
    "btif",
    "bz2",
    "bzip2",
    "cab",
    "caf",
    "cgm",
    "class",
    "cmx",
    "cpio",
    "cr2",
    "cur",
    "dat",
    "dcm",
    "deb",
    "dex",
    "djvu",
    "dll",
    "dmg",
    "dng",
    "doc",
    "docm",
    "docx",
    "dot",
    "dotm",
    "dra",
    "DS_Store",
    "dsk",
    "dts",
    "dtshd",
    "dvb",
    "dwg",
    "dxf",
    "ecelp4800",
    "ecelp7470",
    "ecelp9600",
    "egg",
    "eol",
    "eot",
    "epub",
    "exe",
    "f4v",
    "fbs",
    "fh",
    "fla",
    "flac",
    "flatpak",
    "fli",
    "flv",
    "fpx",
    "fst",
    "fvt",
    "g3",
    "gh",
    "gif",
    "graffle",
    "gz",
    "gzip",
    "h261",
    "h263",
    "h264",
    "icns",
    "ico",
    "ief",
    "img",
    "ipa",
    "iso",
    "jar",
    "jpeg",
    "jpg",
    "jpgv",
    "jpm",
    "jxr",
    "key",
    "ktx",
    "lha",
    "lib",
    "lvp",
    "lz",
    "lzh",
    "lzma",
    "lzo",
    "m3u",
    "m4a",
    "m4v",
    "mar",
    "mdi",
    "mht",
    "mid",
    "midi",
    "mj2",
    "mka",
    "mkv",
    "mmr",
    "mng",
    "mobi",
    "mov",
    "movie",
    "mp3",
    "mp4",
    "mp4a",
    "mpeg",
    "mpg",
    "mpga",
    "mxu",
    "nef",
    "npx",
    "numbers",
    "nupkg",
    "o",
    "odp",
    "ods",
    "odt",
    "oga",
    "ogg",
    "ogv",
    "otf",
    "ott",
    "pages",
    "pbm",
    "pcx",
    "pdb",
    "pdf",
    "pea",
    "pgm",
    "pic",
    "png",
    "pnm",
    "pot",
    "potm",
    "potx",
    "ppa",
    "ppam",
    "ppm",
    "pps",
    "ppsm",
    "ppsx",
    "ppt",
    "pptm",
    "pptx",
    "psd",
    "pya",
    "pyc",
    "pyo",
    "pyv",
    "qt",
    "rar",
    "ras",
    "raw",
    "resources",
    "rgb",
    "rip",
    "rlc",
    "rmf",
    "rmvb",
    "rpm",
    "rtf",
    "rz",
    "s3m",
    "s7z",
    "scpt",
    "sgi",
    "shar",
    "snap",
    "sil",
    "sketch",
    "slk",
    "smv",
    "snk",
    "so",
    "stl",
    "suo",
    "sub",
    "swf",
    "tar",
    "tbz",
    "tbz2",
    "tga",
    "tgz",
    "thmx",
    "tif",
    "tiff",
    "tlz",
    "ttc",
    "ttf",
    "txz",
    "udf",
    "uvh",
    "uvi",
    "uvm",
    "uvp",
    "uvs",
    "uvu",
    "viv",
    "vob",
    "war",
    "wav",
    "wax",
    "wbmp",
    "wdp",
    "weba",
    "webm",
    "webp",
    "whl",
    "wim",
    "wm",
    "wma",
    "wmv",
    "wmx",
    "woff",
    "woff2",
    "wrm",
    "wvx",
    "xbm",
    "xif",
    "xla",
    "xlam",
    "xls",
    "xlsb",
    "xlsm",
    "xlsx",
    "xlt",
    "xltm",
    "xltx",
    "xm",
    "xmind",
    "xpi",
    "xpm",
    "xwd",
    "xz",
    "z",
    "zip",
    "zipx"
  };
}
