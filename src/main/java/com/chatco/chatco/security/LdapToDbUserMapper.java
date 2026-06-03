package com.chatco.chatco.security;

import com.chatco.chatco.entity.AppUser;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
/**
 * Maps a successfully authenticated LDAP user into Spring Security and the
 * local ChatCo user table.
 */
public class LdapToDbUserMapper implements UserDetailsContextMapper {

    private final UserProvisioningService userProvisioningService;
    private final UserAuthorityService userAuthorityService;

    public LdapToDbUserMapper(UserProvisioningService userProvisioningService,
                              UserAuthorityService userAuthorityService) {
        this.userProvisioningService = userProvisioningService;
        this.userAuthorityService = userAuthorityService;
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

        return User.withUsername(appUser.getUsername())
                .password("{noop}N/A")
                .authorities(userAuthorityService.authoritiesForUser(appUser))
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
