package com.chatco.chatco.security;

import com.chatco.chatco.entity.AppUser;
import com.chatco.chatco.model.UserRole;
import com.chatco.chatco.model.UserRoleMapper;
import com.chatco.chatco.repository.AppUserRepository;
import com.chatco.chatco.repository.UserRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class UserAuthorityService {

    private static final String AUTHENTICATED_USER_AUTHORITY = "ROLE_USER";

    private final AppUserRepository appUserRepository;
    private final UserRoleRepository userRoleRepository;

    public UserAuthorityService(AppUserRepository appUserRepository,
                                UserRoleRepository userRoleRepository) {
        this.appUserRepository = appUserRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public Collection<GrantedAuthority> authoritiesForUsername(String username) {
        return appUserRepository.findByUsername(username)
                .map(this::authoritiesForUser)
                .orElseGet(this::baseAuthorities);
    }

    public Collection<GrantedAuthority> authoritiesForUser(AppUser user) {
        if (user.getId() == null) {
            return baseAuthorities();
        }

        Set<String> authorityNames = new LinkedHashSet<>();
        authorityNames.add(AUTHENTICATED_USER_AUTHORITY);

        var roles = userRoleRepository.findRoleNamesByUserId(user.getId()).stream()
                .map(UserRoleMapper::fromDatabaseName)
                .distinct()
                .toList();

        if (roles.isEmpty()) {
            roles = java.util.List.of(UserRole.MITARBEITER);
        }

        roles.stream()
                .map(UserRoleMapper::springAuthority)
                .forEach(authorityNames::add);

        return authorityNames.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private Collection<GrantedAuthority> baseAuthorities() {
        return java.util.List.of(new SimpleGrantedAuthority(AUTHENTICATED_USER_AUTHORITY));
    }
}
