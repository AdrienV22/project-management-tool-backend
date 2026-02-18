package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser_shouldPersistAndReturnId() {
        User user = new User("TestUser", "testuser@example.com", "password123", User.UserRole.MEMBRE);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("TestUser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        assertThat(savedUser.getUserRole()).isEqualTo(User.UserRole.MEMBRE);
    }
}
