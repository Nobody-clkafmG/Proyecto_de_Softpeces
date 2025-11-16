package com.softpeces.auth;

public class AuthService {
    private final UserRepository repo = new UserRepository();

    public static final class Session {
        public final int userId;
        public final String username;
        public final boolean isAdmin;
        public final boolean isOperador;
        Session(int userId, String username, boolean isAdmin, boolean isOperador) {
            this.userId = userId; this.username = username;
            this.isAdmin = isAdmin; this.isOperador = isOperador;
        }
    }

    public Session login(String username, String password) throws Exception {
        var u = repo.findByUsername(username);
        if (u == null || !u.active()) throw new Exception("Usuario no encontrado o inactivo");
        if (!repo.verifyPassword(password, u.passHash())) throw new Exception("Contraseña inválida");
        boolean admin = repo.hasRole(u.id(), "ADMIN");
        boolean op    = repo.hasRole(u.id(), "OPERADOR");
        return new Session(u.id(), u.username(), admin, op);
    }
}
