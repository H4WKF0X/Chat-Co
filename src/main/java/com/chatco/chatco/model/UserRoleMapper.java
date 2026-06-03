package com.chatco.chatco.model;

import java.util.List;

/**
 * Normalizes application role names between the database, the domain model, and
 * Spring Security authorities.
 */
public final class UserRoleMapper {

    private UserRoleMapper() {
    }

    public static UserRole fromDatabaseName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return UserRole.MITARBEITER;
        }

        return switch (roleName.trim().toUpperCase()) {
            case "ADMINISTRATOR", "ADMIN" -> UserRole.ADMINISTRATOR;
            case "MITARBEITER", "EMPLOYEE" -> UserRole.MITARBEITER;
            case "GAST", "GUEST" -> UserRole.GAST;
            default -> UserRole.MITARBEITER;
        };
    }

    public static List<String> databaseNamesFor(UserRole role) {
        return switch (role) {
            case ADMINISTRATOR -> List.of("ADMINISTRATOR", "ADMIN");
            case MITARBEITER -> List.of("MITARBEITER", "EMPLOYEE");
            case GAST -> List.of("GAST", "GUEST");
        };
    }

    public static String springAuthority(UserRole role) {
        return "ROLE_" + role.name();
    }
}
