package com.chatco.chatco.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.*;

@Service
public class LdapAuthService {

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String baseDn;

    private static final String USER_DN_PATTERN = "uid=%s,ou=people,%s"; // anpassen falls nötig

    public boolean authenticate(String username, String password) {
        return bind(getUserDn(username), password) != null;
    }

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
        } catch (Exception e) {
            return null;
        } finally {
            try { ctx.close(); } catch (Exception ignored) {}
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
        } catch (Exception e) {
            return null;
        }
    }

    private String getAttr(Attributes attrs, String name) throws Exception {
        Attribute a = attrs.get(name);
        return a == null ? null : String.valueOf(a.get());
    }

    private List<String> getMulti(Attributes attrs, String name) throws Exception {
        Attribute a = attrs.get(name);
        if (a == null) return List.of();
        List<String> out = new ArrayList<>();
        NamingEnumeration<?> all = a.getAll();
        while (all.hasMore()) out.add(String.valueOf(all.next()));
        return out;
    }
}
