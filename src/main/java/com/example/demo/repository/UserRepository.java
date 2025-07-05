package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository; // for structuring and organizing the data access logic

public interface UserRepository extends JpaRepository<User, Long> {
}
