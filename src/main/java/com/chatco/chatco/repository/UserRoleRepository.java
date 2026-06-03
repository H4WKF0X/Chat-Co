package com.chatco.chatco.repository;

import com.chatco.chatco.entity.UserRole;
import com.chatco.chatco.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Database access for assigning roles to users.
 */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(Long userId);

    @Query("select r.name from UserRole ur join ur.role r where ur.user.id = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    List<UserRole> findByRoleId(Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
