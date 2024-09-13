/*************************************************************************
 * This file is part of CodeOps Studio.
 * CodeOps Studio - code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
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
 
   package com.eup.codeopsstudio.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import com.eup.codeopsstudio.common.AsyncTask;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;

/**
 * A simple server for previewing and streaming contents
 *
 * <p>
 * <li>Use wifi hotspot connection where this device with server connects to a hotspot.
 * <li>Use celluar data connection. Although restrictions may cause a fail.
 * <li>Use device local designate IP address.
 * <li>Last resort is localhost
 *
 * @author EUP
 */
public class LiveServer {

  public Server server;
  private Context context;
  private int port = 0;
  private ServerSocket socket;
  private String rootFolder;
  private File mFile;
  private String fileName;

  public LiveServer(Context context) {
    this.context = context;
  }

  public void setFile(File file) {
    setFile(file.getAbsolutePath());
  }

  /**
   * Specify the file you want to host
   *
   * @param dir the file directory
   */
  public void setFile(String dir) {
    if (dir == null) return;
    mFile = new File(dir);
    this.rootFolder = (String) dir.subSequence(0, dir.lastIndexOf("/"));
    this.fileName = dir.substring(dir.lastIndexOf("/") + 1);
  }
  
  public interface onServerStarted {
    void onStarted(Boolean successful, Throwable throwable);
  }

  /**
   * Starts a live server async
   *
   * <p>Reports a throwable and the url to {@code onServerStarted}
   *
   * @param listener callback for liveserver
   */
  public void start(onServerStarted listener) {
    AsyncTask.runNonCancelable(
        () -> {
          String deviceIp = getWifiOrDeviceIP();
          InetAddress inet = InetAddress.getByName(deviceIp);
          byte[] bytes = inet.getAddress();
          try {
            socket = new ServerSocket(port, 0, InetAddress.getByAddress(bytes));
            port = socket.getLocalPort();
            socket.close(); // Close the socket to initialize the HttpServer
            server = new Server(socket.getInetAddress().getHostAddress(), port);
            server.start();
            return true; // Task completed successfully
          } catch (IOException e) {
            e.printStackTrace();
            return false; // Task failed
          } finally {
            if (socket != null && !socket.isClosed()) {
              try {
                socket.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        },
        (successful, throwable) -> {
          // @server initialization is complete
          listener.onStarted(successful, throwable);
        });
  }

  public void start() {
    try {
      server = new Server();
      server.start();
      port = server.getListeningPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  public String getUrl() {
    if (socket != null) {
      return "http://"
          + socket.getInetAddress().getHostAddress()
          + ":"
          + port
          + "/"
          + fileName;
    } else {
      return "http://" + "localhost" + ":" + port + "/" + fileName;
    }
  }

  public String getAddress() {
    if (socket != null) {
      return "http://" + socket.getInetAddress().getHostAddress() + ":" + port + "/";
    } else {
      return "http://" + "localhost" + ":" + port + "/";
    }
  }

  public class Server extends NanoHTTPD {

    public Server() {
      super("localhost", 2005);
    }

    public Server(String hostname, int port) {
      super(hostname, port);
    }

    @Override
    public NanoHTTPD.Response serve(IHTTPSession session) {
      String uri = session.getUri();
      if (uri.endsWith("/")) {
        uri += fileName;
      }
      String filePath = rootFolder + uri;

      try {
        if (new File(filePath).exists()) {
          FileInputStream fis = new FileInputStream(filePath);

          int contentLength = fis.available();

          return newFixedLengthResponse(
              NanoHTTPD.Response.Status.OK, getMimeTypeForFile(filePath), fis, contentLength);
        } else {
          return newFixedLengthResponse(
              NanoHTTPD.Response.Status.NOT_FOUND,
              NanoHTTPD.MIME_PLAINTEXT,
              "Server failed to serve file: "
                  + filePath
                  + " is not hostable");
        }
      } catch (IOException e) {
        e.printStackTrace();
        return newFixedLengthResponse(
            NanoHTTPD.Response.Status.INTERNAL_ERROR,
            NanoHTTPD.MIME_PLAINTEXT,
            "Server failed to serve session error: " + e.getLocalizedMessage());
      }
    }
  }

  private String getDeviceIpAddress() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    // Check for network connectivity
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Network network = connectivityManager.getActiveNetwork();
      if (network != null) {
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        if (linkProperties != null) {
          for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
            InetAddress address = linkAddress.getAddress();
            if (!address.isLoopbackAddress()) {
              // Return the IP address as a String
              return address.getHostAddress();
            }
          }
        }
      }
    }

    // Fallback to "localhost" if the IP address cannot be determined
    return "localhost";
  }

  public String getWifiIpAddress() {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    if (wifiManager != null && wifiManager.isWifiEnabled()) {
      WifiInfo wifiInfo = wifiManager.getConnectionInfo();
      int ipAddress = wifiInfo.getIpAddress();

      if (ipAddress != 0) {
        try {
          // Try IPv4 first
          InetAddress inetAddress =
              InetAddress.getByAddress(
                  ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipAddress).array());
          if (!inetAddress.isLoopbackAddress()) {
            return inetAddress.getHostAddress();
          }

          // Fallback to IPv6 if IPv4 not found
          return getIPv6Address();
        } catch (UnknownHostException e) {
          e.printStackTrace();
        }
      }
    }

    return null; // WiFi interface not found or IP address not assigned
  }

  private static String getIPv6Address() {
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();
        if (networkInterface.isUp()) {
          Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (!address.isLoopbackAddress() && address instanceof Inet6Address) {
              return address.getHostAddress();
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getWifiOrDeviceIP() {
    String ipAddress = getWifiIpAddress(); // Prioritize Wi-Fi IP address
    if (ipAddress == null) {
      ipAddress = getDeviceIpAddress(); // Fallback to device IP address if Wi-Fi is unavailable
    }
    return ipAddress;
  }

  public File getFile() {
    return mFile;
  }
}
