/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2025 Etido Peter
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

package com.eup.codeopsstudio.util;

import com.eup.codeopsstudio.common.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
 * @author Etido Peter
 */
public class BinaryFileChecker {

    /**
     * File magic numbers are the first bits (HEX BASED) of a file which are used as a unique
     * reference when identifying the type of file.
     *
     * <p>By using the file magic number, detecting the file type is faster because we do not
     * have to
     * search a complex file structure in other to do so. However plain text files have no magic
     * number although they may contain a byte order mark.
     */
    public static final String[] FILE_MAGIC_NUMBERS = {
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
        "CA FE D0 0D", // CAFEBABE. When compressed with Pack200 the bytes
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
    public static final String[] BINARY_FILE_FORMATS = {
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
    public static final String[] TEXT_FILE_EXTENSIONS = {
        // === Plain Text & Logs ===
        "msg_txt",
        "msg_text",
        "msg_log",
        "msg_lst",
        "msg_readme",
        "msg_me",
        "msg_nfo",
        "msg_diz",
        "msg_rtf",
        // === Localization & Translation ===
        "msg_po",
        "msg_pot",
        "msg_mo",
        "msg_strings",

        // === Subtitles & Captions ===
        "msg_srt",
        "msg_vtt",
        "msg_ssa",
        "msg_ass",

        // === Markup & Documentation ===
        // Markdown & similar
        "msg_md",
        "msg_markdown",
        "msg_mkd",
        "msg_mkdn",
        "msg_mdown",
        "msg_mdx",
        "msg_rst",
        "msg_adoc",
        "msg_asciidoc",
        "msg_org",
        // TeX/LaTeX & Bibliography
        "msg_tex",
        "msg_ltx",
        "msg_latex",
        "msg_sty",
        "msg_cls",
        "msg_bib",
        // HTML, XML & related
        "msg_html",
        "msg_htm",
        "msg_xhtml",
        "msg_shtml",
        "msg_xml",
        "msg_xsd",
        "msg_xsl",
        "msg_xslt",
        "msg_svg",
        // DITA
        "msg_dita",
        "msg_ditamap",

        // === Configuration & Data Files ===
        "msg_json",
        "msg_json5",
        "msg_jsonc",
        "msg_yaml",
        "msg_yml",
        "msg_toml",
        "msg_ini",
        "msg_cfg",
        "msg_conf",
        "msg_cnf",
        "msg_cf",
        "msg_env",
        "msg_properties",
        "msg_prop",
        "msg_prefs",
        "msg_editorconfig",
        "msg_csv",
        "msg_tsv",
        "msg_psv",
        "msg_sql",
        "msg_db",
        "msg_dbml",
        "msg_plist",
        "msg_mcmeta",
        "msg_eslintignore",
        "msg_prettierrc",
        "msg_jshintrc",
        "msg_babelrc",
        "msg_npmrc",
        "msg_yarnrc",

        // === Scripting & Programming Languages ===
        // -- C / C++ / Arduino
        "msg_c",
        "msg_h",
        "msg_cpp",
        "msg_cc",
        "msg_cxx",
        "msg_hpp",
        "msg_hh",
        "msg_ino",
        // -- Java (source files only; note that msg_class and msg_jar are binary)
        "msg_java",
        // -- JavaScript / ECMAScript
        "msg_js",
        "msg_jsx",
        "msg_mjs",
        "msg_cjs",
        // -- TypeScript
        "msg_ts",
        "msg_tsx",
        // -- Python (and Cython)
        "msg_py",
        "msg_pyw",
        "msg_pyx",
        "msg_pxd",
        // -- Ruby
        "msg_rb",
        "msg_rake",
        "msg_erb",
        // -- PHP
        "msg_php",
        "msg_php3",
        "msg_php4",
        "msg_php5",
        "msg_phtml",
        // -- Fortran
        "msg_f",
        "msg_for",
        "msg_f90",
        "msg_f95",
        "msg_f03",
        // -- D Language
        "msg_d",
        // -- Hardware Description Languages
        "msg_v",
        "msg_vhd",
        "msg_vhdl",
        "msg_sv",
        "msg_svh",
        // -- Nim
        "msg_nim",
        "msg_nimble",
        // -- Ada
        "msg_adb",
        "msg_ads",

        // -- Pascal
        "msg_pas",
        // -- msg_NET languages
        "msg_cs",
        "msg_fs",
        "msg_fsi",
        "msg_fsx",
        "msg_vb",
        "msg_vbs",
        // -- Go, Dart, Swift, Rust, Kotlin, Scala
        "msg_go",
        "msg_dart",
        "msg_swift",
        "msg_rs",
        "msg_kt",
        "msg_kts",
        "msg_scala",
        "msg_sc",
        // -- Ambiguous: Objective‑C vsmsg_ MATLAB (both are text‑based)
        "msg_m",
        "msg_mm",
        // -- Perl
        "msg_pl",
        "msg_pm",
        "msg_t",
        // -- Shell / Batch / Command Scripts
        "msg_sh",
        "msg_bash",
        "msg_zsh",
        "msg_fish",
        "msg_csh",
        "msg_tcsh",
        "msg_bat",
        "msg_cmd",
        "msg_ps1",
        "msg_psm1",
        "msg_psc1",
        // -- Lua, R, Julia
        "msg_lua",
        "msg_r",
        "msg_rmd",
        "msg_jl",
        // -- Haskell
        "msg_hs",
        "msg_lhs",
        // -- Erlang & Elixir
        "msg_erl",
        "msg_hrl",
        "msg_ex",
        "msg_exs",
        // -- Lisp & Scheme
        "msg_lisp",
        "msg_lsp",
        "msg_cl",
        "msg_scm",
        "msg_rkt",
        // -- OCaml & Reason
        "msg_ml",
        "msg_mli",
        "msg_re",
        "msg_rei",
        // -- Assembly & Related
        "msg_asm",
        "msg_s",
        "msg_nasm",
        "msg_ms",
        "msg_agc",
        // -- Other / Niche Languages
        "msg_jsonnet",
        "msg_clj",
        "msg_cljs",
        "msg_cljc",
        "msg_edn",
        "msg_agda",
        "msg_lean",
        "msg_pde",
        "msg_scad",
        "msg_coffee",
        "msg_litcoffee",

        // === Build & Project Files (mostly text‑based configuration) ===
        "msg_sln",
        "msg_csproj",
        "msg_vbproj",
        "msg_proj",
        "msg_xproj",
        "msg_nuspec",
        "msg_props",
        "msg_targets",
        "msg_config",
        "msg_Rproj",
        "msg_cabal",

        // === Infrastructure / DevOps / Scripting Tools ===
        "msg_hcl",
        "msg_tf",
        "msg_tfvars",
        "msg_sls",
        "msg_nix",
        "msg_gradle",
        "msg_groovy",
        "msg_cake",

        // === Web & Stylesheets ===
        "msg_css",
        "msg_scss",
        "msg_less",
        "msg_sass",
        "msg_styl",
        "msg_ejs",
        "msg_hbs",
        "msg_pug",
        "msg_jade",
        "msg_vue",
        "msg_svelte",
        "msg_razor",
        "msg_rhtml",

        // === Template & Macro Files ===
        "msg_tmpl",
        "msg_tpl",
        "msg_mustache",
        "msg_jinja",
        "msg_jinja2",
        "msg_twig",
        "msg_njk",
        "msg_liquid",

        // === Graph/Data Modeling Formats ===
        "msg_dot",
        "msg_gv",
        "msg_gml",
        "msg_gpx",
        "msg_uml",
        "msg_plantuml",
        "msg_puml",
        "msg_pml",

        // === Other Text‑Based Formats ===
        "msg_ps",
        "msg_eps",
        // PostScript formats
        "msg_ics",
        "msg_ical",
        "msg_ifb",
        // iCalendar formats
        "msg_vcf",
        "msg_vcard",
        // vCard formats
        "msg_torrent",
        "msg_magnet",
        "msg_ged",
        // GEDCOM

        // === Scientific & Data Processing Formats ===
        "msg_mat",
        "msg_sci",
        "msg_sce",
        // MATLAB/Scilab (note: "msg_m" is already used above)
        "msg_rdata",
        "msg_rda",
        // R data files
        "msg_gcode",
        "msg_nc",
        // CNC & 3D printing

        // === Miscellaneous ===
        "msg_todo",
        "msg_taskpaper",
        "msg_rem",
        "msg_gp",
        "msg_gnu",
        // Gnuplot scripts
        "msg_dotsettings",
        // Visual Studio settings
        "msg_gitignore",
        "msg_gitattributes",
        "msg_gitmodules",
        "msg_htaccess",
        "msg_htpasswd",

        // === Additional & Niche Formats ===
        "msg_scpt",
        "msg_applescript",
        "msg_osascript",
        // AppleScript
        "msg_xaml",
        // XAML UI definitions
        "msg_umple",
        // Umple model files
        "msg_wxml",
        // WeChat mini‑program XML
        "msg_ftl",
        // FreeMarker templates
        "msg_txl"
        // TXL language files
    };
    /**
     * Tweaks affects performance, larger read bytes improves precision with pefromance as a
     * trade off
     * and vice versa
     */
    private static final int MAX_READ_BYTES = 4096;
    private static final int BYTE_MASK = 0xFF;
    // weights are based on accuracy, precision, performance
    private static final double ENTROPY_THRESHOLD = 6.5; // Adjusted from 7.2
    private static final int MAGIC_NUMBER_WEIGHT = 6;
    private static final int ENTROPY_WEIGHT = 4;
    private static final int BINARY_CONTENT_WEIGHT = 2;
    private static final int FILE_EXTENSION_WEIGHT = 1;
    private static final int WEIGHT_THRESHOLD = 6;
    // Common control characters
    private static final byte HORIZONTAL_TAB = 0x09;
    private static final byte BACKSPACE = 0x08;
    private static final byte LINE_FEED = 0x0A;
    private static final byte CARRIAGE_RETURN = 0x0D;
    private static final byte SHIFT_OUT = 0x0E;
    // ASCII characters
    private static final byte SPACE = 0x20;
    private static final byte DELETE = 0x7F;
    private static final byte TILDE = 0x7E;
    // Consider binary characters 0x00 -> 0x1F, suspicious characters 0x80 to 0xFF
    private static final byte NULL = 0x00;

    private BinaryFileChecker() {
        // hide constructor
    }

    public static boolean isBinaryFile(File file) throws IOException {
        return isBinaryFile(file, false, MAX_READ_BYTES);
    }

    /**
     * Determines whether a file is binary.
     *
     * @param file            the file to analyze
     * @param useStrictPolicy whether to use a strict policy for file extension checks
     * @param bufferSize      the number of bytes to read from the file for analysis
     * @return true if the file is likely binary, false otherwise
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static boolean isBinaryFile(File file, boolean useStrictPolicy, final int bufferSize) throws
                                                                                                 IOException {
        if (file == null || file.isDirectory() || !file.exists()) {
            throw new IllegalArgumentException(
                "Provided file is either invalid, a directory, or " + "does not exist");
        }

        if (useStrictPolicy) {
            if (isBinaryExtension(file)) {
                return true;
            } else if (isTextExtension(file)) {
                return false; // flag as non-binary
            }
        }

        int score = 0;

        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead = inputStream.read(buffer, 0, bufferSize); // update buffer with bytes

            if (bytesRead < 0) {
                // Possibly an empty file!
                return false;
            }

            if (checkForMagicNumbers(buffer)) {
                score += MAGIC_NUMBER_WEIGHT;
            }

            if (hasHighEntropy(buffer, bytesRead)) {
                int sampleSize = Math.min(bytesRead, MAX_READ_BYTES);
                double weightFactor = Math.min(1.0,
                    sampleSize / 256.0); // 256 = minimum "trusted" size
                score += (int) (ENTROPY_WEIGHT * weightFactor);
            }

            if (containsBinaryContent(buffer, bytesRead)) {
                score += BINARY_CONTENT_WEIGHT;
            }

            if (isBinaryExtension(file)) {
                score += FILE_EXTENSION_WEIGHT;
            }
        }

        return score >= WEIGHT_THRESHOLD;
    }

    private static boolean checkForMagicNumbers(byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return false;
        }

        for (String magicEntry : FILE_MAGIC_NUMBERS) {
            byte[] magicBytes = hexStringToByteArray(magicEntry);
            if (startsWithMagic(buffer, magicBytes)) {
                return true;
            }
        }
        return false; // potential flat binary
    }

    private static byte[] hexStringToByteArray(String s) {
        String[] hexBytes = s.split(" ");
        byte[] data = new byte[hexBytes.length];
        for (int i = 0; i < hexBytes.length; i++) {
            data[i] = (byte) Integer.parseInt(hexBytes[i], 16);
        }
        return data;
    }

    private static boolean startsWithMagic(byte[] buffer, byte[] magic) {
        if (buffer.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if ((buffer[i] & BYTE_MASK) != (magic[i] & BYTE_MASK)) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsBinaryContent(byte[] buffer, final int bytesRead) {

        for (byte b : buffer) {
            int unsigned = b & BYTE_MASK;
            final boolean withHighByteRange = unsigned >= 0x80 && unsigned <= 0xFF;

            if (unsigned == NULL) {
                return true; // Plausible binary content
            }

            if (unsigned == HORIZONTAL_TAB || unsigned == BACKSPACE || unsigned == LINE_FEED
                || unsigned == CARRIAGE_RETURN || unsigned == SHIFT_OUT) {
                continue; // common control characters (non-printable but allowed in text files)
            }

            if (unsigned >= SPACE && unsigned <= TILDE) {
                continue; // ASCII characters, skip
            }

            Charset detectedCharset = EncodingDetector.detectFileEncoding(buffer, bytesRead);

            if (Objects.equals(detectedCharset, StandardCharsets.UTF_8)) {
                if (withHighByteRange) {
                    continue;
                }
            }

            if (withHighByteRange) {
                return true; // not a charset, suspicious binary
            }

            if (unsigned == DELETE) {
                return true; // (non-printable), possibly a binary
            }

            if (unsigned >= 0x01 && unsigned <= 0x1F) {
                return true; // non-printable are likely binary
            }
        }

        return false; // No binary content detected
    }

    private static boolean hasHighEntropy(byte[] buffer, int bytesRead) {
        if (buffer == null || buffer.length == 0) return false;

        int[] frequency = new int[256];
        double entropy = 0.0;
        int sampleSize = Math.min(bytesRead, MAX_READ_BYTES);

        for (int i = 0; i < sampleSize; i++) {
            int unsigned = buffer[i] & BYTE_MASK;
            frequency[unsigned]++;
        }

        for (int freq : frequency) {
            if (freq > 0) {
                double p = (double) freq / sampleSize;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        return entropy >= ENTROPY_THRESHOLD;
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

    private static boolean isTextExtension(File file) {
        String extension = FileUtil.getFileExtension(file);
        if (!Wizard.isEmpty(extension)) {
            for (String textFileExt : TEXT_FILE_EXTENSIONS) {
                if (extension.equalsIgnoreCase(textFileExt)) {
                    return true;
                }
            }
        }
        return false;
    }
}
