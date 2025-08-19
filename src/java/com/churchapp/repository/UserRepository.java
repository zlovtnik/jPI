package com.churchapp.repository;

import com.churchapp.entity.User;
import com.churchapp.entity.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(RoleType role);
    
    List<User> findByActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.member.id = :memberId")
    Optional<User> findByMemberId(@Param("memberId") Integer memberId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
