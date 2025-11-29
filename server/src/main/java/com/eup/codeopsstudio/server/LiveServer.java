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

package com.eup.codeopsstudio.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;

import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.ILog;

import com.eup.codeopsstudio.server.provider.IPProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

/**
 * A simple server for previewing and streaming contents.
 *
 * <p>The server attempts to establish a connection using the following methods in order:
 *
 * <ul>
 *   <li>Use Wi-Fi hotspot connection where this device (with the server) connects to a hotspot.
 *   <li>Use cellular data connection. Note that restrictions may cause this to fail.
 *   <li>Use the device's local designated IP address.
 *   <li>As a last resort, use localhost.
 * </ul>
 *
 * @author Etido Peter
 */
public class LiveServer {

  public static final String TAG = "LiveServer";
  private static final String DEFAULT_DEVICE_NAME = "CodeOps-Mobile::LiveServer";
  private final Context context;
  private Server server;
  private int port = 0;
  private String socketHostAddress = null;
  private String deviceName = DEFAULT_DEVICE_NAME;
  private File sourceDir;
  private File sourceFile = null;
  private boolean singleFileMode = false;
  private String fileName;
  private IPProvider ipProvider;

  public LiveServer(@NonNull Context context) {
    this.context = context.getApplicationContext();
    this.ipProvider = new IPProvider(context);
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public File getSourceFile() {
    return sourceFile;
  }

  public String getUrl() {
    String url = null;
    if (singleFileMode) {
      url = getAddress() + (fileName == null || fileName.isEmpty() ? "" : fileName);
    } else {
      url = getAddress();
    }
    return url;
  }

  public String getAddress() {
    return "http://"
        + Objects.requireNonNullElse(socketHostAddress, Server.LOCAL_HOST)
        + ":"
        + port
        + "/";
  }

  public boolean isAlive() {
    return server != null && server.isAlive();
  }

  /**
   * Starts a live server with dynamic host address.
   *
   * <p>Dynamic host address implies either local-host, Wifi or Device
   *
   * <p><strong>This is a thread blocking call</strong>
   *
   * @throws IOException if an I/O error occurs
   */
  public void launch() throws IOException {
    String deviceIP = ipProvider.getIP();
    if (deviceIP == null) {
      deviceIP = "127.0.0.1";
    }

    ILog.info(TAG, "Launching server with IP: " + deviceIP);

    byte[] address = InetAddress.getByName(deviceIP).getAddress();
    InetAddress bindAddress = InetAddress.getByAddress(address);
    ServerSocket socket = null;

    try {
      socket = new ServerSocket(0, 0, bindAddress);
      socket.setReuseAddress(true);
      port = socket.getLocalPort();
      socketHostAddress = socket.getInetAddress().getHostAddress();
      socket.close(); // Close to instantiate the Server class

      try {
        Thread.sleep(50); // ensure port release
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Server startup interrupted", e);
      }
      server = new Server(socketHostAddress, port);
      server.start();
      ILog.info(TAG, "Server started on: " + getUrl());
    } catch (Throwable th) {
      throw new IOException(th);
    } finally {
      if (socket != null && !socket.isClosed()) {
        try {
          socket.close();
          socket = null;
        } catch (IOException e) {
          ILog.error(TAG, "Failed to close server socket", e);
        }
      }
    }
  }

  public void launchWithLocalHost() {
    try {
      server = new Server();
      server.start();
      port = server.getListeningPort();
    } catch (IOException e) {
      ILog.error(TAG, "Error occurred when starting server", e);
    }
  }

  public void setDirectoryMode(@NonNull File directory) {
    Objects.requireNonNull(directory, "Source directory must not be null");
    if (!directory.exists() || !directory.isDirectory()) {
      throw new IllegalArgumentException("Provided directory is invalid");
    }
    this.singleFileMode = false;
    this.sourceFile = null;
    this.sourceDir = directory;
    this.fileName = ""; // no fileName for directory root
    ILog.info(TAG, "setDirectoryMode called");
  }

  public void setSingleFileMode(@NonNull File file) {
    Objects.requireNonNull(file, "Source file must not be null");
    if (!file.exists() || !file.isFile()) {
      throw new IllegalArgumentException("Provided file is invalid");
    }
    File parent = file.getParentFile();
    if (parent == null) {
      throw new IllegalArgumentException("Parent folder could not be determined for: " + file);
    }
    this.singleFileMode = true;
    this.sourceFile = file;
    this.sourceDir = parent;
    this.fileName = file.getName();
    ILog.info(TAG, "setSingleFileMode called");
  }

  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  class Server extends NanoHTTPD {
    protected static final String LOCAL_HOST = "localhost";
    private static final int DEFAULT_PORT = 2005;

    public Server() {
      this(DEFAULT_PORT);
    }

    public Server(int port) {
      this(LOCAL_HOST, port);
    }

    public Server(String hostName, int port) {
      super(hostName, port);
    }

    @Override
    public Response serve(@NonNull IHTTPSession session) {
      String uri = session.getUri();
      ILog.info(TAG, "Received request for URI: " + uri);

      if (singleFileMode) {
        if ("/".equals(uri) || uri.isEmpty()) {
          if (sourceFile == null || !sourceFile.exists()) {
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "Server not " + "configured with a source file.");
          }
          return serveFile(sourceFile);
        }

        // For other requests, serve files from the source directory
        File requestedFile = new File(sourceDir, uri);

        if (!isSafePath(requestedFile)) {
          return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Forbidden");
        }

        if (requestedFile.exists() && requestedFile.isFile()) {
          return serveFile(requestedFile);
        } else {
          return newFixedLengthResponse(
              Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found: " + uri);
        }
      }

      File requestedFile = new File(sourceDir, uri);

      if (!isSafePath(sourceDir, requestedFile)) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Forbidden");
      }

      try {
        if (sourceDir != null
            && !requestedFile.getCanonicalPath().startsWith(sourceDir.getCanonicalPath())) {
          ILog.warning(TAG, "Directory traversal attempt detected for URI: " + uri);
          return newFixedLengthResponse(
              Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Forbidden: Access Denied");
        }
      } catch (IOException e) {
        ILog.error(TAG, "Error resolving canonical path for: " + requestedFile.getPath(), e);
        return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR,
            MIME_PLAINTEXT,
            "Error " + "500: Internal Server Error");
      }

      if (!requestedFile.exists()) {
        return newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            MIME_PLAINTEXT,
            "Error 404: File or directory not found: " + requestedFile.getAbsolutePath());
      }

      if (requestedFile.isDirectory()) {
        return serveDirectory(requestedFile);
      } else {
        return serveFile(requestedFile);
      }
    }

    @NonNull
    private Response serveDirectory(@NonNull File directory) {
      Objects.requireNonNull(directory, "Directory must not be null");
      ILog.info(TAG, "Serving Directory: " + directory.getName());

      StringBuilder htmlBuilder = new StringBuilder();
      htmlBuilder.append("<!DOCTYPE html>\n");
      htmlBuilder.append("<html lang=\"en\">\n");
      htmlBuilder.append("<head>\n");
      htmlBuilder.append("    <meta charset=\"UTF-8\">\n");
      htmlBuilder.append(
          "    <meta name=\"viewport\" content=\"width=device-width, " + "initial-scale=1.0\">\n");
      htmlBuilder.append("    <title>").append(deviceName).append(" - File Listing</title>\n");
      htmlBuilder.append("    <style>\n");
      htmlBuilder.append(
          "        body { font-family: sans-serif; margin: 20px; "
              + "background-color: #f4f4f4; color: #333; }\n");
      htmlBuilder.append("        h1 { color: #0056b3; }\n");
      htmlBuilder.append("        ul { list-style-type: none; padding: 0; }\n");
      htmlBuilder.append(
          "        li { margin-bottom: 8px; background-color: #fff; padding: "
              + "10px; border-radius: 4px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
      htmlBuilder.append("        a { text-decoration: none; color: #007bff; }\n");
      htmlBuilder.append("        a:hover { text-decoration: underline; }\n");
      htmlBuilder.append("    </style>\n");
      htmlBuilder.append("</head>\n");
      htmlBuilder.append("<body>\n");
      htmlBuilder
          .append("    <h1>")
          .append(deviceName)
          .append(" - Contents of ")
          .append(directory.getName())
          .append("</h1>\n");
      htmlBuilder.append("    <ul>\n");

      File[] filesList = directory.listFiles();
      if (filesList != null) {
        for (File file : filesList) {
          // Ensure the relative path is correctly formed for URL, especially for
          // subdirectories
          String relativePath =
              file.getAbsolutePath().substring(directory.getAbsolutePath().length());
          if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
          }
          htmlBuilder
              .append("        <li><a href=\"")
              .append(relativePath)
              .append("\">")
              .append(file.getName())
              .append(file.isDirectory() ? "/" : "")
              .append("</a></li>\n");
        }
      }

      htmlBuilder.append("    </ul>\n");
      htmlBuilder.append("</body>\n");
      htmlBuilder.append("</html>\n");

      return newFixedLengthResponse(Response.Status.OK, "text/html", htmlBuilder.toString());
    }

    private boolean isSafePath(File requestedFile) {
      return isSafePath(requestedFile.getParentFile(), requestedFile);
    }

    private boolean isSafePath(File srcDir, File requestedFile) {
      try {
        if (srcDir != null) {
          String reqPath = requestedFile.getCanonicalPath();
          String srcPath = srcDir.getCanonicalPath();
          return reqPath.startsWith(srcPath);
        }
      } catch (IOException e) {
        ILog.error(TAG, "Error checking if requestedFile path is safe", e);
        return false;
      }
      return true;
    }

    @NonNull
    private Response serveFile(@NonNull File file) {
      ILog.info(TAG, "Serving File: " + file.getName());
      try {
        String mimeType = MimeTypes.getMimeType(file);
        FileInputStream fis = new FileInputStream(file);
        Response response =
            newFixedLengthResponse(Response.Status.OK, mimeType, fis, file.length());

        // CORS headers for cross-origin requests
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        return response;
      } catch (IOException e) {
        ILog.error(TAG, "Error serving file: " + file.getAbsolutePath(), e);
        return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR,
            MIME_PLAINTEXT,
            "Error 500: Could not serve file. " + e.getMessage());
      }
    }
  }
}
