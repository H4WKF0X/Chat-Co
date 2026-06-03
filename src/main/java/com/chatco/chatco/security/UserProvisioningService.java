package com.chatco.chatco.security;

import com.chatco.chatco.entity.AppUser;
import com.chatco.chatco.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
/**
 * Keeps the local {@link AppUser} table in sync with users who log in through LDAP.
 */
public class UserProvisioningService {

    private final AppUserRepository appUserRepository;

    public UserProvisioningService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    /**
     * Loads an existing local user by LDAP uid or creates one from LDAP profile data.
     *
     * <p>Existing users are updated on login so display name and mail stay in sync
     * with LDAP.</p>
     */
    public AppUser loadOrCreateFromLdap(String ldapUid, String username, String displayName, String mail) {
        Optional<AppUser> existing = appUserRepository.findByLdapUid(ldapUid);

        if (existing.isPresent()) {
            AppUser user = existing.get();

            // Keep local profile data aligned with LDAP on every successful login.
            user.setUsername(username);
            user.setDisplayName(displayName);
            user.setMail(mail);

            return appUserRepository.save(user);
        }

        AppUser user = new AppUser();
        user.setLdapUid(ldapUid);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setMail(mail);

        return appUserRepository.save(user);
    }
}
