package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ FIXED: removed duplicate findByusername (lowercase u)
    // All methods now use Optional for safe null handling

    Optional<User> findByEmailId(String emailId);
    Optional<User> findByUsername(String username);         // ✅ only this one
    Optional<User> findByStaffId(String staffId);
    Optional<User> findByEmailIdAndStaffId(String emailId, String staffId);

    boolean existsByUsername(String username);
    boolean existsByEmailId(String emailId);
    boolean existsByStaffId(String staffId);

    List<User> findByDeletedFalse();
    List<User> findByDeletedTrue();
    List<User> findByApprovedTrue();
    List<User> findByApprovedFalse();
}