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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eup.codeopsstudio.common.ILog;

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

    public LiveServer(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    public static String getIPv4Address() {
        return getIpAddress(true);
    }

    @Nullable
    private static String getIpAddress(boolean preferIPv4) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isLoopbackAddress()) continue;
                    String result = resolvePreferredIp(preferIPv4, address);
                    if (result != null) return result;
                }
            }
        } catch (Exception e) {
            ILog.error(TAG, "Failed to get IP address (IPv4: " + preferIPv4 + ")", e);
        }
        return null;
    }

    @Nullable
    private static String resolvePreferredIp(boolean preferIPv4, InetAddress address) {
        if (preferIPv4 && address instanceof Inet4Address) {
            return address.getHostAddress();
        }
        if (!preferIPv4 && address instanceof Inet6Address) {
            return stripIPv6ZoneIndex(address.getHostAddress());
        }
        return null;
    }

    @NonNull
    private static String stripIPv6ZoneIndex(@Nullable String ip) {
        Objects.requireNonNull(ip, "IP address must not be null");
        int zoneIndex = ip.indexOf('%');
        return (zoneIndex != -1) ? ip.substring(0, zoneIndex) : ip;
    }

    @Nullable
    public static String getIPv6Address() {
        return getIpAddress(false);
    }

    public boolean isAlive() {
        return server != null && server.isAlive();
    }

    /**
     * Starts a live server with dynamic host address.
     * <p>
     * Dynamic host address implies either local-host, Wifi or Device
     * </p>
     * <p> <strong>This is a thread blocking call</strong>
     *
     * @throws IOException if an I/O error occurs
     */
    public void launch() throws IOException {
        String deviceIP = getWifiOrDeviceIP();
        ILog.info(TAG, "Device IP: " + deviceIP);

        byte[] address = InetAddress
            .getByName(deviceIP)
            .getAddress();
        InetAddress bindAddress = InetAddress.getByAddress(address);

        try (ServerSocket socket = new ServerSocket(port, 0, bindAddress)) {
            port              = socket.getLocalPort();
            socketHostAddress = socket
                .getInetAddress()
                .getHostAddress();
        }

        server = new Server(socketHostAddress, port);
        server.start();
    }

    private String getWifiOrDeviceIP() {
        String ipAddress = getWifiIpAddress();
        if (ipAddress == null) {
            ipAddress = getDeviceIpAddress();
        }
        return ipAddress;
    }

    /**
     * @return the device IP if connected to the web or default to localhost
     */
    private String getDeviceIpAddress() {
        var connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Check for network connectivity
        Network network = connectivityManager.getActiveNetwork();
        if (network != null) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
            if (linkProperties != null) {
                for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                    InetAddress address = linkAddress.getAddress();
                    if (!address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        }
        return Server.LOCAL_HOST;
    }

    /**
     * @return the Wifi IP address or null if WiFi interface is not found or IP address not assigned
     */
    @Nullable
    private String getWifiIpAddress() {
        String ipV4 = getIPv4Address();
        if (ipV4 != null) return ipV4;

        String ipV6 = getIPv6Address();
        if (ipV6 != null) return ipV6;

        ILog.warning(TAG, "Failed to get IP V4/V6 Wifi address");
        return null;
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

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    public String getUrl() {
        return getAddress() + (fileName == null || fileName.isEmpty() ? "" : fileName);
    }

    public String getAddress() {
        return "http://" + Objects.requireNonNullElse(socketHostAddress, Server.LOCAL_HOST) + ":"
            + port + "/";
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setSingleFileMode(@NonNull File file) {
        Objects.requireNonNull(file, "Source file must not be null");
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Provided file is invalid");
        }
        File parent = file.getParentFile();
        if (parent == null) {
            throw new IllegalArgumentException(
                "Parent folder could not be determined for: " + file);
        }
        this.singleFileMode = true;
        this.sourceFile     = file;
        this.sourceDir      = parent;
        this.fileName       = file.getName();
    }

    public void setDirectoryMode(@NonNull File directory) {
        Objects.requireNonNull(directory, "Source directory must not be null");
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Provided directory is invalid");
        }
        this.singleFileMode = false;
        this.sourceFile     = null;
        this.sourceDir      = directory;
        this.fileName       = "";  // no fileName for directory root
    }

    class Server extends NanoHTTPD {
        protected static final String LOCAL_HOST = "localhost";
        private static final int DEFAULT_PORT = 2005;

        public Server() {
            this(LOCAL_HOST, DEFAULT_PORT);
        }

        public Server(String hostName, int port) {
            super(hostName, port);
        }

        public Server(int port) {
            this(LOCAL_HOST, port);
        }

        @Override
        public Response serve(@NonNull IHTTPSession session) {
            if (singleFileMode) {
                if (sourceFile == null) {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                        "Server not " + "configured with a source file.");
                }
                ILog.info(TAG, "Serving file: " + sourceFile.getName());
                return serveFile(sourceFile);
            }

            String uri = session.getUri();
            ILog.info(TAG, "Received request for URI: " + uri);
            File requestedFile = new File(sourceDir, uri);

            try {
                if (sourceDir != null && !requestedFile
                    .getCanonicalPath()
                    .startsWith(sourceDir.getCanonicalPath())) {
                    ILog.warning(TAG, "Directory traversal attempt detected for URI: " + uri);
                    return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT,
                        "Forbidden: Access Denied");
                }
            } catch (IOException e) {
                ILog.error(TAG,
                    "Error resolving canonical path for: " + requestedFile.getPath(), e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "Error " + "500: Internal Server Error");
            }

            if (!requestedFile.exists()) {
                ILog.warning(TAG, "File not found: " + requestedFile.getAbsolutePath());
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
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

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n");
            htmlBuilder.append("<html lang=\"en\">\n");
            htmlBuilder.append("<head>\n");
            htmlBuilder.append("    <meta charset=\"UTF-8\">\n");
            htmlBuilder.append("    <meta name=\"viewport\" content=\"width=device-width, "
                + "initial-scale=1.0\">\n");
            htmlBuilder
                .append("    <title>")
                .append(deviceName)
                .append(" - File Listing</title>\n");
            htmlBuilder.append("    <style>\n");
            htmlBuilder.append("        body { font-family: sans-serif; margin: 20px; "
                + "background-color: #f4f4f4; color: #333; }\n");
            htmlBuilder.append("        h1 { color: #0056b3; }\n");
            htmlBuilder.append("        ul { list-style-type: none; padding: 0; }\n");
            htmlBuilder.append("        li { margin-bottom: 8px; background-color: #fff; padding: "
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
                    String relativePath = file
                        .getAbsolutePath()
                        .substring(directory
                            .getAbsolutePath()
                            .length());
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

        @NonNull
        private Response serveFile(@NonNull File file) {
            try {
                String mimeType = getMime(file);
                FileInputStream fis = new FileInputStream(file);
                return newFixedLengthResponse(Response.Status.OK, mimeType, fis, file.length());
            } catch (IOException e) {
                ILog.error(TAG, "Error serving file: " + file.getAbsolutePath(), e);
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                    "Error 500: Could not serve file. " + e.getMessage());
            }
        }

        @NonNull
        private String getMime(@NonNull File file) {
            String mimeType = getMimeTypeForFile(file.getName());
            if (mimeType == null) mimeType = "application/octet-stream";
            return mimeType;
        }
    }
}