package com.eup.codeops.common.plugin.api;

public final class Contract {
  public static final String ACTION_PLUGIN_REGISTER =
      "com.eup.codeops.common.plugin.api.action.PLUGIN_REGISTER";

  public static final String EXTRA_PLUGIN_ID = "plugin_id";
  public static final String EXTRA_PLUGIN_NAME ="plugin_name";
  public static final String EXTRA_PLUGIN_AUTHOR ="plugin_author";
  public static final String EXTRA_PLUGIN_DESCRIPTION ="plugin_description";
  public static final String EXTRA_MIN_KERNEL_VERSION = "min_kernel_version";
  public static final String EXTRA_SUPPORTED_EXTENSIONS = "supported_extensions";
  public static final String EXTRA_DOWNLOAD_URL = "download_url";

  private Contract() {
    // No instances
  }
}
