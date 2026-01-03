/*
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

package com.eup.codeopsstudio.server.provider;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.eup.codeopsstudio.common.ILog;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Internet-Protocol provider
 *
 * <p>This class provides the IP address when connected to a network via WiFi or Internet Connection
 * (private & public)
 *
 * @author Etido Peter
 */
public class IPProvider {

  public static final String TAG = "IPProvider";

  private final Context context;
  private final ConnectivityManager connectivityManager;

  public IPProvider(@NonNull Context context) {
    this.context = context;
    this.connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public String getIP() {
    if (isConnected()) {
      if (isConnectedToWifi()) {
        ILog.info(TAG, "Connected to WiFi");
        return getWifiIPAddress();
      } else if (isConnectedToMobileData()) {
        ILog.info(TAG, "Connected to Mobile Data");
        return getDeviceIpAddress();
      }
    }
    ILog.info(TAG, "No internet connection");
    return null;
  }

  public boolean isConnected() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      NetworkCapabilities capabilities =
          connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
      return capabilities != null
          && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
              || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
              || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    } else {
      NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.isConnected();
    }
  }

  public boolean isConnectedToWifi() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      NetworkCapabilities capabilities =
          connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
      return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    } else {
      NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
  }

  public boolean isConnectedToMobileData() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      NetworkCapabilities capabilities =
          connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
      return capabilities != null
          && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    } else {
      NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
      return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
    }
  }

  @Nullable
  private String getDeviceIpAddress() {
    Network network = connectivityManager.getActiveNetwork();
    if (network != null) {
      LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
      if (linkProperties != null) {
        for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
          InetAddress address = linkAddress.getAddress();
          /*
          if (!address.isLoopbackAddress()) {
            return address.getHostAddress();
          }
          */
          // Filter out loopback and link-local (fe80::)
          if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
            if (address instanceof Inet4Address) {
              return address.getHostAddress();
            }
          }
        }
      }
    }
        
    ILog.warning(TAG, "Failed to get IP address for data connection");
    return null;
  }
  
  @Nullable
  private String getWifiIPAddress() {
    String ipV4 = getPreferredIP(true);
    if (ipV4 != null) return ipV4;

    String ipV6 = getPreferredIP(false);
    if (ipV6 != null) return ipV6;

    ILog.warning(TAG, "Failed to get IP address for WiFi connection");
    return null;
  }

  @Nullable
  private static String getPreferredIP(boolean preferIPv4) {
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
          // Filter out LinkLocal addresses (fe80::)
          if (address.isLinkLocalAddress()) continue;
          
          String result = resolvePreferredIP(preferIPv4, address);
          if (result != null) return result;
        }
      }
    } catch (Exception e) {
      ILog.error(TAG, "Failed to get IP address (IPv4: " + preferIPv4 + ")", e);
    }
    return null;
  }

  @Nullable
  private static String resolvePreferredIP(boolean preferIPv4, InetAddress address) {
    if (preferIPv4 && address instanceof Inet4Address) {
      String addrs = address.getHostAddress();
      ILog.info(TAG, "IPV4 Address: " + addrs + ", real: " + (addrs.indexOf(':') < 0));
      return addrs;
    }

    if (!preferIPv4 && address instanceof Inet6Address) {
      String addrs = stripIPv6ZoneIndex(address.getHostAddress());
      ILog.info(TAG, "IPV6 Address: " + addrs);
      return addrs;
    }
    
    return null;
  }

  @NonNull
  private static String stripIPv6ZoneIndex(@NonNull String ip) {
    Objects.requireNonNull(ip, "IPV6 address must not be null");
    int zoneIndex = ip.indexOf('%');
    return (zoneIndex != -1) ? ip.substring(0, zoneIndex) : ip;
  }
}
