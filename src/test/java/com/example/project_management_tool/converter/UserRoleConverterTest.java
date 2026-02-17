package com.example.project_management_tool.converter;

import com.example.project_management_tool.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleConverterTest {

    private final UserRoleConverter converter = new UserRoleConverter();

    @Test
    void convertToDatabaseColumn_shouldReturnNull_whenRoleIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnValue_whenRoleIsNotNull() {
        // on prend un rôle existant, sans dépendre d'une valeur précise
        User.UserRole role = User.UserRole.values()[0];

        Integer dbValue = converter.convertToDatabaseColumn(role);

        assertEquals(role.getValue(), dbValue);
    }

    @Test
    void convertToEntityAttribute_shouldReturnNull_whenValueIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_shouldReturnRole_whenValueMatches() {
        User.UserRole expected = User.UserRole.values()[0];

        User.UserRole actual = converter.convertToEntityAttribute(expected.getValue());

        assertEquals(expected, actual);
    }

    @Test
    void convertToEntityAttribute_shouldThrow_whenValueDoesNotMatchAnyRole() {
        // On choisit une valeur qui a de fortes chances de ne pas exister.
        // Même si un jour un rôle "999" existait, tu peux ajuster à -1.
        int invalidValue = 999;

        assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute(invalidValue));
    }
}
