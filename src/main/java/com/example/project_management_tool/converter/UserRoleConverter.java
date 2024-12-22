package com.example.project_management_tool.converter;

import com.example.project_management_tool.entity.User;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<User.UserRole, Integer> {

    @Override
    public Integer convertToDatabaseColumn(User.UserRole role) {
        if (role == null) {
            return null;
        }
        return role.getValue(); // On utilise la méthode getValue() de l'énumération
    }

    @Override
    public User.UserRole convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        for (User.UserRole role : User.UserRole.values()) {
            if (role.getValue() == value) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid value for UserRole: " + value);
    }
}
