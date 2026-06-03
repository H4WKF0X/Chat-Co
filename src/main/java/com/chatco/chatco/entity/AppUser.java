package com.chatco.chatco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.PrePersist;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "app_user")
/**
 * Local application user.
 *
 * <p>Users usually come from LDAP, but the application keeps a local row so
 * messages, meetings, roles, and uploaded files can reference a stable user id.</p>
 */
public class AppUser {
    /** Timestamp when the user was first stored locally. */
    @NotNull
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Allows accounts to be disabled without deleting related data. */
    @NotNull
    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** E-mail address copied from LDAP when available. */
    @Size(max = 255)
    @Column(name = "mail")
    private String mail;

    /** Human readable name shown in the UI. */
    @Size(max = 150)
    @NotNull
    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;
    @Size(max = 100)
    @NotNull
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /** Stable LDAP identifier used to match future LDAP logins to this row. */
    @Size(max = 255)
    @Column(name = "ldap_uid")
    private String ldapUid;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Applies Java-side defaults before inserting a new user.
     */
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

}
