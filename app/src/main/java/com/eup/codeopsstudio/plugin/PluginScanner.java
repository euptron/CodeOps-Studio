package com.eup.codeopsstudio.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import com.eup.codeops.common.plugin.api.Contract;
import com.eup.codeops.common.plugin.api.Type;
import com.eup.codeops.common.plugin.api.sandbox.Plugin;
import com.eup.codeops.intellisense.plugin.api.IntellisenseServiceConnection;
import com.eup.codeopsstudio.common.ILog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PluginScanner {

  private static final String TAG = "PluginScanner";

  private final Context context;
  private final Map<String, Plugin<?>> registeredPlugins = new HashMap<>();
  private final Map<String, ConnectionState> connectionStates = new HashMap<>();

  private static PluginScanner instance;

  public static PluginScanner get(Context context) {
    if (instance == null) {
      instance = new PluginScanner(context.getApplicationContext());
    }
    return instance;
  }

  private PluginScanner(Context context) {
    this.context = context;
  }

  public List<PluginItem> scan() {
    List<PluginItem> pluginItems = new ArrayList<>();

    ILog.info(TAG, "Scanning for available plugins...");
    connectionStates.clear();
    registeredPlugins.clear();

    PackageManager pm = context.getPackageManager();
    Intent intent = new Intent(Contract.ACTION_PLUGIN_REGISTER);
    List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, PackageManager.GET_META_DATA);

    for (ResolveInfo resolveInfo : resolveInfos) {
      try {
        Plugin<?> plugin = extractPlugin(resolveInfo);
        if (plugin != null) {
          if (supportsPluginVersion(plugin)) {
            registeredPlugins.put(plugin.getId(), plugin);
            
            pluginItems.add(
                new PluginItem(
                    plugin.getId(),
                    plugin.getName(),
                    plugin.getAuthor(),
                    resolveInfo.loadIcon(pm),
                    plugin.getDescription()));
            ILog.debug(TAG, "Found plugin: " + plugin.getName() + " (" + plugin.getId() + ")");
          } else {
            ILog.warning(TAG, "Plugin requires newer app version: " + plugin.getName());
          }
        }
      } catch (Exception e) {
        ILog.error(TAG, "Failed to parse plugin: " + resolveInfo.serviceInfo.name, e);
      }
    }

    return pluginItems == null ? new ArrayList<>() : pluginItems;
  }

  private Plugin<?> extractPlugin(ResolveInfo resolveInfo) {
    try {
      ServiceInfo svc = resolveInfo.serviceInfo;
      String packageName = svc.packageName;
      String componentName = svc.name;
      Bundle meta = svc.metaData;
      // Meta data
      Type type = Type.APK;
      String id = meta.getString(Contract.EXTRA_PLUGIN_ID);
      String name = meta.getString(Contract.EXTRA_PLUGIN_NAME);
      String author = meta.getString(Contract.EXTRA_PLUGIN_AUTHOR, "Unknown");
      String desc = meta.getString(Contract.EXTRA_PLUGIN_DESCRIPTION, "");
      int minKernel = meta.getInt(Contract.EXTRA_MIN_KERNEL_VERSION, 1);
      String downloadUrl = meta.getString(Contract.EXTRA_DOWNLOAD_URL, "");
      String extRaw = meta.getString(Contract.EXTRA_SUPPORTED_EXTENSIONS, "");
      String[] exts = extRaw.split(",");

      for (int i = 0; i < exts.length; i++) {
        exts[i] = exts[i].trim();
      }

      if (id == null || name == null) {
        ILog.warning(TAG, "Invalid plugin metadata in package: " + packageName);
        return null;
      }

      var cap = new PluginCapabilities(packageName, componentName);
      return new Plugin<>(cap, type, id, name, author, desc, downloadUrl, minKernel, exts);
    } catch (Exception e) {
      ILog.error(TAG, "Error extracting plugin info", e);
      return null;
    }
  }

  private boolean supportsPluginVersion(Plugin<?> plugin) {
    int supportVersion = getSupportVersion();
    if (plugin.getMinKernelVersion() < supportVersion) return false;
    return true;
  }

  private int getSupportVersion() {
    // Return current plugin system version,  TODO: better implement versioning logic here
    return 1;
  }

  public List<Plugin<?>> getAvailablePlugins() {
    return new ArrayList<>(registeredPlugins.values());
  }

  public Plugin<?> getPlugin(String id) {
    return registeredPlugins.get(id);
  }

  public boolean isRegistered(String id) {
    return registeredPlugins.containsKey(id);
  }

  public boolean isActive(String id) {
    ConnectionState state = connectionStates.get(id);
    return state != null && state.bound;
  }

  public boolean isConnected(String id) {
    ConnectionState state = connectionStates.get(id);
    if (state == null) return false;
    IntellisenseServiceConnection connection = state.connection;
    return connection != null && connection.isConnected();
  }

  public IntellisenseServiceConnection getConnection(String id) {
    var state = connectionStates.get(id);
    return state.connection;
  }

  public List<Plugin<?>> getPluginsForExtension(String extension) {
    List<Plugin<?>> matchingPlugins = new ArrayList<>();

    for (Plugin<?> plugin : registeredPlugins.values()) {
      String[] supportedExtensions = plugin.getSupportedExtensions();
      for (String supportedExt : supportedExtensions) {
        if (extension.equalsIgnoreCase(supportedExt.trim())) {
          matchingPlugins.add(plugin);
          break;
        }
      }
    }

    return matchingPlugins;
  }

  public void clearPlugins() {
    registeredPlugins.clear();
    connectionStates.clear();
  }

  public boolean attachPlugin(
      String id, int port, IntellisenseServiceConnection.ConnectionCallback callback) {
    if (!isRegistered(id)) {
      ILog.warning(TAG, "Cannot attach an unregistered plugin: " + id);
      return false;
    }

    if (isActive(id)) {
      ILog.info(TAG, "Plugin already attached: " + id);
      return true;
    }

    try {
      Plugin<?> plugin = getPlugin(id);
      if (plugin == null) return false;

      var intent = new Intent(Contract.ACTION_PLUGIN_REGISTER);
      var cap = (PluginCapabilities) plugin.getCapabilities();
      intent.setClassName(cap.packageName, cap.componentName);

      ILog.debug(TAG, "Attempting to bind to: " + cap.packageName + "/" + cap.componentName);
      ILog.debug(TAG, "Intent action: " + intent.getAction());

      context.startForegroundService(intent);

      // wait a bit for service to initialize
      try {
        Thread.sleep(1000); // ms
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      var connection = new IntellisenseServiceConnection(callback);

      boolean bound =
          context.bindService(
              intent,
              connection,
              Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT | Context.BIND_EXTERNAL_SERVICE);

      if (bound) {
        var connectionState = new ConnectionState(true, connection);
        connectionStates.put(id, connectionState);
        // start the intellisense
        connection.start(port);
        ILog.info(TAG, "Successfully attached plugin: " + id);
        return true;
      }
      ILog.debug(TAG, "bindService returned: " + bound);
      return bound;
    } catch (SecurityException e) {
      ILog.error(TAG, "Security exception binding to plugin: " + id, e);
      return false;
    } catch (Exception e) {
      ILog.error(TAG, "Error connecting to plugin: " + id, e);
      return false;
    }
  }

  public void detachAllPlugins() {
    for (String pluginId : connectionStates.keySet()) {
      if (detachPlugin(pluginId)) {
        ILog.info(TAG, "Plugin: " + pluginId + " detached successfully");
      } else {
        ILog.warning(TAG, "could not detach plugin : " + pluginId);
      }
    }
  }

  public boolean detachPlugin(String id) {
    if (!isRegistered(id)) {
      ILog.warning(TAG, "Cannot detach an unegistered plugin: " + id);
      return false;
    }

    try {
      Plugin<?> plugin = getPlugin(id);
      if (plugin == null) return false;

      var state = connectionStates.get(id);
      IntellisenseServiceConnection connection = state.connection;

      if (connection != null) {
        // context.unbindService(connection);
      }

      var intent = new Intent(Contract.ACTION_PLUGIN_REGISTER);
      var cap = (PluginCapabilities) plugin.getCapabilities();
      intent.setClassName(cap.packageName, cap.componentName);
      context.stopService(intent);
      connectionStates.remove(id);
      registeredPlugins.remove(id);
      ILog.debug(TAG, "Successfully detached plugin: " + id);
      return true;
    } catch (Exception e) {
      ILog.error(TAG, "Error detaching plugin", e);
      return false;
    }
  }

  class PluginCapabilities {
    final String packageName;
    final String componentName;

    public PluginCapabilities(String packageName, String componentName) {
      this.packageName = packageName;
      this.componentName = componentName;
    }
  }

  class ConnectionState {
    final boolean bound;
    final IntellisenseServiceConnection connection;

    public ConnectionState(boolean bound, IntellisenseServiceConnection connection) {
      this.bound = bound;
      this.connection = connection;
    }
  }
}
