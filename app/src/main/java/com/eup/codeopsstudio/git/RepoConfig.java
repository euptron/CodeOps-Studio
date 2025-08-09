package com.eup.codeopsstudio.git;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

public class RepoConfig {

    private final UserConfig administrator;
    private File sshKey;
    private String passphrase;
    private String name;
    private String remoteURI;
    private String localURI;
    private List<UserConfig> contributors;

    public RepoConfig(UserConfig administrator) {
        this.administrator = administrator;
    }

    @NonNull
    public static String extractRepoNameFromUri(@NonNull String url) {
        int lastSlashIndex = url.lastIndexOf("/");
        if (lastSlashIndex >= 0 && lastSlashIndex < url.length() - 1) {
            String repoName = url.substring(lastSlashIndex + 1);
            if (repoName.endsWith(".git")) {
                return repoName.substring(0, repoName.length() - 4);
            }
        }
        return "";
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public File getSshKey() {
        return sshKey;
    }

    public void setSshKey(File sshKey) {
        this.sshKey = sshKey;
    }

    @NonNull
    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserConfig getAdministrator() {
        return administrator;
    }

    public List<UserConfig> getContributors() {
        return contributors;
    }

    public void setContributors(List<UserConfig> contributors) {
        this.contributors = contributors;
    }

    public String getRemoteURI() {
        return remoteURI;
    }

    public void setRemoteURI(String remoteURI) {
        this.remoteURI = remoteURI;
    }

    public String getLocalURI() {
        return localURI;
    }

    public void setLocalURI(String localURI) {
        this.localURI = localURI;
    }
}
