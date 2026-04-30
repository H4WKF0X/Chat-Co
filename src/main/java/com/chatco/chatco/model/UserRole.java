package com.chatco.chatco.model;

/**
 * The role assigned to a user account, controlling access to admin features.
 *
 * <ul>
 *   <li>{@code ADMINISTRATOR} – full access including the admin dashboard</li>
 *   <li>{@code MITARBEITER} – standard employee account</li>
 *   <li>{@code GAST} – guest account with limited permissions</li>
 * </ul>
 */
public enum UserRole {
    ADMINISTRATOR, MITARBEITER, GAST
}
