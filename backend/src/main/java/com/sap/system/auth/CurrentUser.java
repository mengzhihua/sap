package com.sap.system.auth;

public final class CurrentUser {
    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static void set(String username) {
        USER.set(username);
    }

    public static String get() {
        return USER.get();
    }

    public static void clear() {
        USER.remove();
    }
}
