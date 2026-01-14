package com.cinema.application.users;

import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.Username;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogger auditLogger;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordPolicy passwordPolicy,
            AuditLogger auditLogger
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }


    public User register(String rawUsername, String rawPassword, String fullName) {


        String normalizedUsername = rawUsername == null ? null : rawUsername.trim();
        String normalizedFullName = fullName == null ? null : fullName.trim();

        Username username = Username.of(normalizedUsername);

        if (userRepository.existsByUsername(username)) {

            throw new DuplicateException("username", "Username already exists");
        }


        passwordPolicy
                .validate(rawPassword, username, normalizedFullName)
                .ensureValid();

        HashedPassword hashedPassword = HashedPassword.fromRaw(rawPassword);


        User user = new User(
                null,
                username,
                hashedPassword,
                normalizedFullName,
                BaseRole.USER,
                false,
                0,
                null,
                null
        );

        User saved = userRepository.Save(user);

        auditLogger.logAction(
                saved.id(),
                "REGISTER_REQUESTED",
                "Account created INACTIVE (awaiting ADMIN activation)"
        );

        return saved;
    }
}
