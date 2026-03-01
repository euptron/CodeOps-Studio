package com.eup.codeops.common.plugin.api.sandbox;

import android.os.Parcel;
import android.os.Parcelable;
import com.eup.codeops.common.plugin.api.Type;
import java.util.Objects;

public final class Plugin<C> implements Parcelable {

  private final C capabilities;
  private final Type type;
  private final String id;
  private final String name;
  private final String author;
  private final String description;
  private final String downloadUrl;
  private final int minKernelVersion;
  private final String[] supportedExtensions;

  public Plugin(
      C capabilities,
      Type type,
      String id,
      String name,
      String author,
      String description,
      String downloadUrl,
      int minKernelVersion,
      String[] supportedExtensions) {
    this.capabilities = capabilities;
    this.type = type;
    this.id = id;
    this.name = name;
    this.author = author;
    this.description = description;
    this.downloadUrl = downloadUrl;
    this.minKernelVersion = minKernelVersion;
    this.supportedExtensions =
        supportedExtensions != null ? supportedExtensions.clone() : new String[0];
  }

  public C getCapabilities() {
    return this.capabilities;
  }

  public Type getType() {
    return this.type;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getAuthor() {
    return this.author;
  }

  public String getDescription() {
    return this.description;
  }

  public String getDownloadUrl() {
    return this.downloadUrl;
  }

  public int getMinKernelVersion() {
    return this.minKernelVersion;
  }

  public String[] getSupportedExtensions() {
    return this.supportedExtensions.clone();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable((Parcelable) capabilities, flags);
    dest.writeString(type.name());
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(author);
    dest.writeString(description);
    dest.writeString(downloadUrl);
    dest.writeInt(minKernelVersion);
    dest.writeStringArray(supportedExtensions);
  }

  protected Plugin(Parcel in) {
    this.capabilities = (C) in.readParcelable(getClass().getClassLoader());
    this.type = Type.valueOf(in.readString());
    this.id = in.readString();
    this.name = in.readString();
    this.author = in.readString();
    this.description = in.readString();
    this.downloadUrl = in.readString();
    this.minKernelVersion = in.readInt();
    this.supportedExtensions = in.createStringArray();
  }

  public static final Creator<Plugin> CREATOR =
      new Creator<Plugin>() {
        @Override
        public Plugin createFromParcel(Parcel source) {
          return new Plugin(source);
        }

        @Override
        public Plugin[] newArray(int size) {
          return new Plugin[size];
        }
      };

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Plugin<?> plugin = (Plugin<?>) o;
    return minKernelVersion == plugin.minKernelVersion
        && type == plugin.type
        && id.equals(plugin.id)
        && name.equals(plugin.name)
        && author.equals(plugin.author)
        && description.equals(plugin.description)
        && downloadUrl.equals(plugin.downloadUrl)
        && java.util.Arrays.equals(supportedExtensions, plugin.supportedExtensions);
  }

  @Override
  public int hashCode() {
    int result =
        Objects.hash(
            capabilities, type, id, name, author, description, downloadUrl, minKernelVersion);
    result = 31 * result + java.util.Arrays.hashCode(supportedExtensions);
    return result;
  }

  @Override
  public String toString() {
    return "Plugin["
        + "capabilities="
        + capabilities
        + ", type="
        + type
        + ", id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", author='"
        + author
        + '\''
        + ", description='"
        + description
        + '\''
        + ", downloadUrl='"
        + downloadUrl
        + '\''
        + ", minKernelVersion="
        + minKernelVersion
        + ", supportedExtensions="
        + java.util.Arrays.toString(supportedExtensions)
        + ']';
  }
}
