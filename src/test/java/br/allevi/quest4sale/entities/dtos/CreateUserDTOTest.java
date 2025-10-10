package br.allevi.quest4sale.entities.dtos;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateUserDTO Validation Tests")
class CreateUserDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid CreateUserDTO with all required fields")
    void shouldCreateValidCreateUserDTO() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO should have no validation violations");
    }

    @Test
    @DisplayName("Should create valid CreateUserDTO without optional avatarUrl")
    void shouldCreateValidCreateUserDTOWithoutAvatarUrl() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Valid DTO without avatarUrl should have no validation violations");
    }

    @Test
    @DisplayName("Should fail validation when username is blank")
    void shouldFailValidationWhenUsernameIsBlank() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("")
                .email("test@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with blank username should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username") && 
                              v.getMessage().contains("must not be blank")),
                "Should have username blank violation");
    }

    @Test
    @DisplayName("Should fail validation when username is null")
    void shouldFailValidationWhenUsernameIsNull() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username(null)
                .email("test@example.com")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with null username should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username") && 
                              v.getMessage().contains("must not be blank")),
                "Should have username blank violation");
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void shouldFailValidationWhenEmailIsBlank() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with blank email should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") && 
                              v.getMessage().contains("must not be blank")),
                "Should have email blank violation");
    }

    @Test
    @DisplayName("Should fail validation when email is null")
    void shouldFailValidationWhenEmailIsNull() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email(null)
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with null email should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") && 
                              v.getMessage().contains("must not be blank")),
                "Should have email blank violation");
    }

    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void shouldFailValidationWhenEmailFormatIsInvalid() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("invalid-email")
                .password("password123")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with invalid email format should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") && 
                              v.getMessage().contains("must be a well-formed email address")),
                "Should have email format violation");
    }

    @Test
    @DisplayName("Should fail validation when password is blank")
    void shouldFailValidationWhenPasswordIsBlank() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("")
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with blank password should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") && 
                              v.getMessage().contains("must not be blank")),
                "Should have password blank violation");
    }

    @Test
    @DisplayName("Should fail validation when password is null")
    void shouldFailValidationWhenPasswordIsNull() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password(null)
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with null password should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") && 
                              v.getMessage().contains("must not be blank")),
                "Should have password blank violation");
    }

    @Test
    @DisplayName("Should fail validation when password is too short")
    void shouldFailValidationWhenPasswordIsTooShort() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("1234567") // 7 characters, minimum is 8
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with short password should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") && 
                              v.getMessage().contains("size must be between 8 and 100")),
                "Should have password size violation");
    }

    @Test
    @DisplayName("Should fail validation when password is too long")
    void shouldFailValidationWhenPasswordIsTooLong() {
        // Given
        String longPassword = "a".repeat(101); // 101 characters, maximum is 100
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password(longPassword)
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "DTO with long password should have validation violations");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") && 
                              v.getMessage().contains("size must be between 8 and 100")),
                "Should have password size violation");
    }

    @Test
    @DisplayName("Should accept password at minimum length")
    void shouldAcceptPasswordAtMinimumLength() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("12345678") // exactly 8 characters
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "DTO with password at minimum length should be valid");
    }

    @Test
    @DisplayName("Should accept password at maximum length")
    void shouldAcceptPasswordAtMaximumLength() {
        // Given
        String maxPassword = "a".repeat(100); // exactly 100 characters
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password(maxPassword)
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "DTO with password at maximum length should be valid");
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats() {
        String[] validEmails = {
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "user123@example-domain.com",
                "user@sub.example.com"
        };

        for (String email : validEmails) {
            // Given
            CreateUserDTO dto = CreateUserDTO.builder()
                    .username("testuser")
                    .email(email)
                    .password("password123")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

            // Then
            assertTrue(violations.isEmpty(), 
                    "DTO with valid email '" + email + "' should have no violations");
        }
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void shouldRejectInvalidEmailFormats() {
        String[] invalidEmails = {
                "invalid-email",
                "@example.com",
                "user@",
                "user..name@example.com",
                "user@.com",
                "user@example..com",
                "user name@example.com"
        };

        for (String email : invalidEmails) {
            // Given
            CreateUserDTO dto = CreateUserDTO.builder()
                    .username("testuser")
                    .email(email)
                    .password("password123")
                    .build();

            // When
            Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

            // Then
            assertFalse(violations.isEmpty(), 
                    "DTO with invalid email '" + email + "' should have violations");
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email")),
                    "Should have email validation violation for: " + email);
        }
    }

    @Test
    @DisplayName("Should handle multiple validation errors")
    void shouldHandleMultipleValidationErrors() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("") // blank username
                .email("invalid-email") // invalid email format
                .password("123") // too short password
                .build();

        // When
        Set<ConstraintViolation<CreateUserDTO>> violations = validator.validate(dto);

        // Then
        assertEquals(3, violations.size(), "Should have 3 validation violations");
        
        // Check specific violations
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    @DisplayName("Should test Lombok builder functionality")
    void shouldTestLombokBuilderFunctionality() {
        // Given & When
        CreateUserDTO dto = CreateUserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();

        // Then
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
        assertEquals("https://example.com/avatar.jpg", dto.getAvatarUrl());
    }

    @Test
    @DisplayName("Should test Lombok getters and setters")
    void shouldTestLombokGettersAndSetters() {
        // Given
        CreateUserDTO dto = new CreateUserDTO();

        // When
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setAvatarUrl("https://example.com/avatar.jpg");

        // Then
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("password123", dto.getPassword());
        assertEquals("https://example.com/avatar.jpg", dto.getAvatarUrl());
    }
}
