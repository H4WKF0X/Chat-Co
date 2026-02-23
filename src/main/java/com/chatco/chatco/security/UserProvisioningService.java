package com.chatco.chatco.security;

import com.chatco.chatco.entity.AppUser;
import com.chatco.chatco.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserProvisioningService {

    private final AppUserRepository appUserRepository;

    public UserProvisioningService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser loadOrCreateFromLdap(String ldapUid, String username, String displayName, String mail) {
        Optional<AppUser> existing = appUserRepository.findByLdapUid(ldapUid);

        if (existing.isPresent()) {
            AppUser user = existing.get();

            // optional: Daten aus LDAP aktualisieren
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

        // falls vorhanden in deiner Entity:
        // user.setIsActive(true);

        return appUserRepository.save(user);
    }
}