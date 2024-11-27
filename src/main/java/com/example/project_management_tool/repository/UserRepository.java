package com.example.project_management_tool.repository;

import com.example.project_management_tool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
