package com.chatco.chatco.service.db;

import com.chatco.chatco.entity.UserRoleId;
import com.chatco.chatco.model.AppUser;
import com.chatco.chatco.model.UserRole;
import com.chatco.chatco.model.UserStatus;
import com.chatco.chatco.repository.AppUserRepository;
import com.chatco.chatco.repository.RoleRepository;
import com.chatco.chatco.repository.UserRoleRepository;
import com.chatco.chatco.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DbUserService implements UserService {

    private final AppUserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final RoleRepository roleRepo;

    public DbUserService(AppUserRepository userRepo, UserRoleRepository userRoleRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.roleRepo = roleRepo;
    }

    @Override
    public List<AppUser> getAll() {
        return userRepo.findAll().stream().map(this::toRecord).toList();
    }

    @Override
    public Optional<AppUser> findById(Long id) {
        return userRepo.findById(id).map(this::toRecord);
    }

    @Override
    public AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .map(this::toRecord)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB: " + username));
    }

    @Override
    @Transactional
    public void updateUser(AppUser updated) {
        userRepo.findById(updated.id()).ifPresent(entity -> {
            entity.setDisplayName(updated.displayName());
            entity.setMail(updated.mail());
            entity.setIsActive(updated.active());
            userRepo.save(entity);
        });
    }

    @Override
    @Transactional
    public void addUser(String username, String displayName, String mail, UserRole role) {
        com.chatco.chatco.entity.AppUser entity = new com.chatco.chatco.entity.AppUser();
        entity.setUsername(username);
        entity.setDisplayName(displayName);
        entity.setMail(mail);
        entity.setIsActive(true);
        com.chatco.chatco.entity.AppUser saved = userRepo.save(entity);

        roleRepo.findByName(role.name()).ifPresent(roleEntity -> {
            com.chatco.chatco.entity.UserRole userRole = new com.chatco.chatco.entity.UserRole();
            userRole.setId(new UserRoleId(saved.getId(), roleEntity.getId()));
            userRole.setUser(saved);
            userRole.setRole(roleEntity);
            userRoleRepo.save(userRole);
        });
    }

    AppUser toRecord(com.chatco.chatco.entity.AppUser entity) {
        UserRole role = userRoleRepo.findByUserId(entity.getId()).stream()
                .findFirst()
                .map(ur -> {
                    try {
                        return UserRole.valueOf(ur.getRole().getName());
                    } catch (IllegalArgumentException e) {
                        return UserRole.MITARBEITER;
                    }
                })
                .orElse(UserRole.MITARBEITER);
        UserStatus status = Boolean.TRUE.equals(entity.getIsActive()) ? UserStatus.ACTIVE : UserStatus.INACTIVE;
        return new AppUser(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getMail(),
                Boolean.TRUE.equals(entity.getIsActive()),
                status,
                role,
                entity.getCreatedAt()
        );
    }
}
