package com.eup.codeopsstudio.git;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;

public record UserConfig(String userName, String email, char[] password, boolean isAdmin) {
    public UserConfig(String userName, char[] password) {
        this(userName, null, password, true);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserConfig gitUser = (UserConfig) o;
        return isAdmin == gitUser.isAdmin && Objects.equals(email, gitUser.email)
            && Objects.equals(userName, gitUser.userName)
            && Objects.deepEquals(password, gitUser.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, Arrays.hashCode(password), email, isAdmin);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserConfig{" + "userName='" + userName + '\'' + ", password="
            + Arrays.toString(password) + ", email='" + email + '\'' + ", isAdmin=" + isAdmin + '}';
    }
}