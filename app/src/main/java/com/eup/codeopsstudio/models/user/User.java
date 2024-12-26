package com.eup.codeopsstudio.models.user;

import android.os.Bundle;
import com.eup.codeopsstudio.IdeApplication;
import com.eup.codeopsstudio.domain.FormatDateUseCase;
import com.eup.codeopsstudio.util.Wizard;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class User {

  private final String uniqueID;
  private final String firstLaunchTimeID;
  private final DeviceInfo deviceInfo;
  private final Locale preferredLocale;
  private final String preferredDateFormat;

  private static final List<String> PREFERRED_DATE_FORMATS =
      List.of(
          "yyyy-MM-dd HH:mm:ss",
          "dd-MM-yyyy HH:mm:ss",
          "HH:mm:ss dd-MM-yyyy",
          "EEE, dd-MMM-yyyy HH:mm:ss");

  private static final String DEFAULT_DATE_FORMAT = PREFERRED_DATE_FORMATS.get(3);

  private User() {
    throw new UnsupportedOperationException("User must be created through the factory method.");
  }

  public static User newInstance() {
    var context = IdeApplication.getGlobalContext();
    var deviceInfo = new DeviceInfo(context);
    var uniqueID = Wizard.getUserID(context);

    return new User(
        uniqueID,
        Wizard.getInstallUserID(context),
        deviceInfo,
        deviceInfo.getLocale(),
        DEFAULT_DATE_FORMAT);
  }

  public User(
      String uniqueID,
      String firstLaunchTimeID,
      DeviceInfo deviceInfo,
      Locale preferredLocale,
      String preferredDateFormat) {
    this.uniqueID = uniqueID;
    this.firstLaunchTimeID = firstLaunchTimeID;
    this.deviceInfo = deviceInfo;
    this.preferredLocale = preferredLocale;
    this.preferredDateFormat = preferredDateFormat;
  }

  public static void registerSession() {
    var analytics = IdeApplication.getAnalytics();
    var context = IdeApplication.getGlobalContext();
    var deviceInfo = new DeviceInfo(context);
    var uniqueID = Wizard.getUserID(context);
    var bundle = new Bundle();
    var user = newInstance();

    analytics.setAnalyticsCollectionEnabled(true);
    analytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
    analytics.setUserId(uniqueID);

    bundle.putString("package_name", deviceInfo.getAppPackageName());
    bundle.putInt("app_version_code", deviceInfo.getAppVersionCode());
    bundle.putString("app_version_name", deviceInfo.getAppVersionName());
    bundle.putString("device_model", deviceInfo.getModel());
    bundle.putString("device_board", deviceInfo.getBoard());
    bundle.putString("device_build_id", deviceInfo.getBuildID());
    bundle.putString("device_sdk_version", deviceInfo.getSdkVersion());
    bundle.putString("device_manufacturer", deviceInfo.getBrand());
    bundle.putString("device_release_version", deviceInfo.getRelease());
    bundle.putString("device_cpu_architecture", deviceInfo.getCpuArchitecture());
    bundle.putString("device_locale_country", deviceInfo.getLocaleCountry());
    bundle.putString("device_locale_language", deviceInfo.getLocaleLanguage());
    bundle.putString("device_country", deviceInfo.getCountry());
    bundle.putString(
        "session_date", new FormatDateUseCase(user).format(Calendar.getInstance().getTime()));
    analytics.logEvent("session_start", bundle);
  }

  public String getUniqueID() {
    return this.uniqueID;
  }

  public String getFirstLaunchTimeID() {
    return this.firstLaunchTimeID;
  }

  public DeviceInfo getDeviceInfo() {
    return this.deviceInfo;
  }

  public Locale getPreferredLocale() {
    return this.preferredLocale;
  }

  public String getPreferredDateFormat() {
    return this.preferredDateFormat;
  }

  @Override
  public String toString() {
    return "User[uniqueID="
        + uniqueID
        + ", firstLaunchTimeID="
        + firstLaunchTimeID
        + ", deviceInfo="
        + deviceInfo
        + ", preferredLocale="
        + preferredLocale
        + ", preferredDateFormat="
        + preferredDateFormat
        + "]";
  }
}
