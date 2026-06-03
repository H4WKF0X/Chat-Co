package com.chatco.chatco.repository;

import com.chatco.chatco.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Database access for application users.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByLdapUid(String ldapUid);
    Optional<AppUser> findByMail(String mailAddress);
    boolean existsByUsername(String username);
    boolean existsByLdapUid(String ldapUid);

    /**
     * Case-insensitive search used by the user search API.
     */
    @Query("SELECT u FROM AppUser u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<AppUser> searchByUsernameOrDisplayName(@Param("q") String q);
}
