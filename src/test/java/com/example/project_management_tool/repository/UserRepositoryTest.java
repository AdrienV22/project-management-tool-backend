package com.example.project_management_tool.repository;

import com.example.project_management_tool.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // Import correct

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // Charge le profil "test"
@TestPropertySource(locations = "classpath:application-test.properties") // Assure l'utilisation du fichier de config pour les tests
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Désactive l'utilisation d'une base de données embarquée
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveUser() {
        // Création d'un utilisateur
        User user = new User();
        user.setUsername("TestUser");
        user.setEmail("testuser@example.com");
        user.setPassword("password123");

        // Sauvegarde dans la base
        User savedUser = userRepository.save(user);

        // Assertions pour vérifier les résultats
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("TestUser");
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
    }
}
