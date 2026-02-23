package com.chatco.chatco.repository;

import com.chatco.chatco.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByLdapUid(String ldapUid);

    Optional<AppUser> findByMail(String mailAddress);

    boolean existsByUsername(String username);

    boolean existsByLdapUid(String ldapUid);
}
