package com.softpeces.auth;

public final class AuthContext {
    private static AuthService.Session session;
    public static void set(AuthService.Session s) { session = s; }
    public static AuthService.Session get() { return session; }
    public static String username() { return session == null ? "anon" : session.username; }
}
