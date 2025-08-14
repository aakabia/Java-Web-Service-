package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository; // for structuring and organizing the data access logic
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByEmail(String email);
    boolean existsUserByUsername(String userName);
    Optional<User> findByUsername(String userName);
    Optional<User> findByEmail(String email);
    Optional<User> findByOpaqueToken(String opaqueToken);
}
