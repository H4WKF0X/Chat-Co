package com.chatco.chatco.security;

import com.chatco.chatco.entity.AppUser;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
/**
 * Maps a successfully authenticated LDAP user into Spring Security and the
 * local ChatCo user table.
 */
public class LdapToDbUserMapper implements UserDetailsContextMapper {

    private final UserProvisioningService userProvisioningService;

    public LdapToDbUserMapper(UserProvisioningService userProvisioningService) {
        this.userProvisioningService = userProvisioningService;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails mapUserFromContext(
            DirContextOperations ctx,
            String username,
            Collection<? extends GrantedAuthority> authorities
    ) {
        String uid = getAttr(ctx, "uid");
        String cn = getAttr(ctx, "cn");
        String mail = getAttr(ctx, "mail");

        if (uid == null || uid.isBlank()) uid = username;
        if (cn == null || cn.isBlank()) cn = username;

        AppUser appUser = userProvisioningService.loadOrCreateFromLdap(uid, username, cn, mail);

        // Roles are currently local application roles. Every LDAP user receives
        // ROLE_USER so authenticated web requests are allowed.
        List<GrantedAuthority> mappedAuthorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        return User.withUsername(appUser.getUsername())
                .password("{noop}N/A")
                .authorities(mappedAuthorities)
                .build();
    }

    @Override
    public void mapUserToContext(
            org.springframework.security.core.userdetails.UserDetails user,
            org.springframework.ldap.core.DirContextAdapter ctx
    ) {
        throw new UnsupportedOperationException("LDAP login only; writing users back to LDAP is not supported");
    }
    private String getAttr(DirContextOperations ctx, String attrName) {
        try {
            String value = ctx.getStringAttribute(attrName);
            return (value != null && !value.isBlank()) ? value : null;
        } catch (Exception e) {
            return null;
        }
    }
}
