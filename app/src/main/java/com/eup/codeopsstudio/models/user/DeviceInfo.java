package com.eup.codeopsstudio.models.user;

import android.content.Context;
import com.eup.codeopsstudio.util.Wizard;
import java.util.Locale;

/**
 * Model representing the device info Collected for analytical purposes
 *
 * @author EUP
 */
public class DeviceInfo {

  private final String appVersionName;
  private final String appPackageName;
  private final int appVersionCode;
  private final String model;
  private final String sdkVersion;
  private final String buildID;
  private final String release;
  private final String board;
  private final String brand;
  private final String cpuArchitecture;
  private final String country;
  private final Locale locale;
  private final String localeLanguage;
  private final String localeCountry;

  public DeviceInfo(Context context) {
    this.appPackageName = Wizard.getAppVersionName(context);
    this.appVersionCode = Wizard.getAppVersionCodeInteger(context);
    this.appVersionName = Wizard.getAppVersionName(context);   
    this.model = Wizard.getDeviceBuildModel();
    this.sdkVersion = Wizard.getDeviceSDKVersion();
    this.buildID = Wizard.getDeviceBuildID();
    this.release = Wizard.getDeviceReleaseVersion();
    this.board = Wizard.getDeviceBoard();
    this.brand = Wizard.getDeviceManuFacturer();
    this.cpuArchitecture = Wizard.getDeviceArchitecture();
    this.country = Wizard.getDeviceCountry(context);
    this.localeLanguage = Wizard.getDeviceLocaleLanguage();
    this.localeCountry = Wizard.getLocaleCountry(context);
    this.locale = new Locale(localeLanguage, localeCountry);
  }

  public String getAppVersionName() {
    return this.appVersionName;
  }

  public String getAppPackageName() {
    return this.appPackageName;
  }

  public int getAppVersionCode() {
    return this.appVersionCode;
  }

  public String getModel() {
    return this.model;
  }

  public String getSdkVersion() {
    return this.sdkVersion;
  }

  public String getBuildID() {
    return this.buildID;
  }

  public String getRelease() {
    return this.release;
  }

  public String getBoard() {
    return this.board;
  }

  public String getBrand() {
    return this.brand;
  }

  public String getCpuArchitecture() {
    return this.cpuArchitecture;
  }

  public String getCountry() {
    return this.country;
  }

  public Locale getLocale() {
    return this.locale;
  }

  public String getLocaleLanguage() {
    return this.localeLanguage;
  }

  public String getLocaleCountry() {
    return this.localeCountry;
  }
}
