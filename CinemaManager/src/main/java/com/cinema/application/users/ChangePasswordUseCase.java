package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.HashedPassword;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.policy.PasswordPolicy;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final AuditLogger auditLogger;

    public ChangePasswordUseCase(
            UserRepository userRepository,
            PasswordPolicy passwordPolicy,
            AuditLogger auditLogger
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }


    @Transactional
    public void changePassword(
            UserId userId,
            String currentPassword,
            String newPassword,
            String newPasswordRepeat
    ) {
        if (userId == null) throw new AuthorizationException("Unauthorized");

        if (isBlank(currentPassword) || isBlank(newPassword) || isBlank(newPasswordRepeat)) {
            throw new ValidationException("User","Password fields are required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));


        if (!user.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }


        user.invalidateSession();


        if (!newPassword.equals(newPasswordRepeat)) {
            userRepository.Save(user);
            auditLogger.logAction(user.id(), "CHANGE_PASSWORD_FAILED", "new_password_mismatch");
            throw new ValidationException("User","New passwords do not match");
        }


        if (!user.password().matches(currentPassword)) {
            user.registerFailedLogin();
            userRepository.Save(user);

            if (!user.isActive()) {
                auditLogger.logAction(user.id(), "CHANGE_PASSWORD_DEACTIVATED", "3_failed_old_password_attempts");
                throw new AuthorizationException("Account deactivated after 3 failed attempts");
            }

            auditLogger.logAction(user.id(), "CHANGE_PASSWORD_FAILED", "invalid_current_password");
            throw new AuthorizationException("Current password is invalid");
        }


        passwordPolicy
                .validate(newPassword, user.username(), user.fullName())
                .ensureValid();

        HashedPassword newHash = HashedPassword.fromRaw(newPassword);


        user.changePassword(newHash);

        userRepository.Save(user);

        auditLogger.logAction(user.id(), "CHANGE_PASSWORD", "success");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
