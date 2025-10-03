package com.mentara.repository;

import com.mentara.entity.User;
import com.mentara.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    //暂时没有用到
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userAuth WHERE u.username = :username")
    Optional<User> findByUsernameWithAuth(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT ua.id FROM UserAuth ua WHERE ua.email = :email)")
    Optional<User> findByEmail(@Param("email") String email);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAuth ua WHERE ua.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    // 按角色统计用户数量
    long countByRole(UserRole role);
    
    // 搜索用户 - 支持按用户名、昵称、邮箱进行模糊搜索（只搜索未删除的用户）
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.userAuth ua WHERE u.isDeleted = false AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(u.nickname, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(ua.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    // 查找未删除的用户
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    Page<User> findAllActiveUsers(Pageable pageable);
    
    // 统计未删除的用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
    long countActiveUsers();
    
    // 查找未删除的用户（按用户名）
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userAuth WHERE u.username = :username AND u.isDeleted = false")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);
    
    // 检查用户名是否存在（包括已删除的用户）
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username")
    boolean existsByUsernameIncludingDeleted(@Param("username") String username);
    
    // 查找所有用户（包括已删除的）
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameIncludingDeleted(@Param("username") String username);
    
    // 批量查询用户信息
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userAuth WHERE u.id IN :userIds")
    List<User> findByIdIn(@Param("userIds") Set<Long> userIds);
    
    // 按角色查询未删除的用户
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isDeleted = false")
    List<User> findByRoleAndIsDeletedFalse(@Param("role") UserRole role);
}