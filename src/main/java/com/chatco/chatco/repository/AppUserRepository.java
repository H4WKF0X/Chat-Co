package com.chatco.chatco.repository;

import com.chatco.chatco.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByLdapUid(String ldapUid);
    Optional<AppUser> findByMail(String mailAddress);
    boolean existsByUsername(String username);
    boolean existsByLdapUid(String ldapUid);
}