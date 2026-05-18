package com.chatco.chatco.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

@Service
public class LdapAuthService {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthService.class);

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String baseDn;

    private static final String USER_DN_PATTERN = "uid=%s,ou=people,%s";

    public boolean authenticate(String username, String password) {
        return bind(getUserDn(username), password) != null;
    }

    /**
     * Authenticates and returns selected LDAP attributes, or {@code null} on failure.
     */
    public Map<String, Object> loginAndFetchProfile(String username, String password) {
        String userDn = getUserDn(username);
        DirContext ctx = bind(userDn, password);
        if (ctx == null) return null;

        try {
            Attributes attrs = ctx.getAttributes(userDn, new String[]{"cn", "displayName", "mail", "memberOf"});
            Map<String, Object> profile = new HashMap<>();
            profile.put("dn", userDn);
            profile.put("cn", getAttr(attrs, "cn"));
            profile.put("displayName", getAttr(attrs, "displayName"));
            profile.put("mail", getAttr(attrs, "mail"));
            profile.put("groups", getMulti(attrs, "memberOf"));
            return profile;
        } catch (NamingException e) {
            log.error("Failed to read LDAP attributes for {}: {}", userDn, e.getMessage());
            return null;
        } finally {
            try { ctx.close(); } catch (NamingException ignored) {}
        }
    }

    private String getUserDn(String username) {
        return String.format(USER_DN_PATTERN, username, baseDn);
    }

    private DirContext bind(String userDn, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            return new InitialDirContext(env);
        } catch (AuthenticationException e) {
            log.debug("LDAP authentication failed for {}", userDn);
            return null;
        } catch (NamingException e) {
            log.warn("LDAP connection error for {}: {}", userDn, e.getMessage());
            return null;
        }
    }

    private String getAttr(Attributes attrs, String name) throws NamingException {
        Attribute a = attrs.get(name);
        return a == null ? null : String.valueOf(a.get());
    }

    private List<String> getMulti(Attributes attrs, String name) throws NamingException {
        Attribute a = attrs.get(name);
        if (a == null) return List.of();
        List<String> out = new ArrayList<>();
        NamingEnumeration<?> all = a.getAll();
        while (all.hasMore()) out.add(String.valueOf(all.next()));
        return out;
    }
}
