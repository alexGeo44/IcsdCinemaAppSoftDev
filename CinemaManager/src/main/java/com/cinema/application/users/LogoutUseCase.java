package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.entity.value.UserId;
import com.cinema.domain.enums.BaseRole;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.AuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class LogoutUseCase {

    private final AuditLogger auditLogger;
    private final UserRepository userRepository;

    public LogoutUseCase(AuditLogger auditLogger, UserRepository userRepository) {
        this.auditLogger = Objects.requireNonNull(auditLogger);
        this.userRepository = Objects.requireNonNull(userRepository);
    }


    @Transactional
    public void logoutSelf(UserId actorId) {
        if (actorId == null) throw new AuthorizationException("Unauthorized");

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User", "User not found"));


        actor.invalidateSession();
        userRepository.Save(actor);

        auditLogger.logAction(actorId, "LOGOUT", "self");
    }


    @Transactional
    public void forceLogout(UserId adminId, UserId targetUserId) {
        if (adminId == null) throw new AuthorizationException("Unauthorized");
        if (targetUserId == null) throw new ValidationException("","targetUserId is required");

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("User", "Admin not found"));

        if (admin.baseRole() != BaseRole.ADMIN) {
            throw new AuthorizationException("Only ADMIN can force logout");
        }


        if (!admin.isActive()) {
            throw new AuthorizationException("Account is inactive");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User", "Target user not found"));

        if (target.baseRole() == BaseRole.ADMIN) {
            throw new AuthorizationException("Cannot force logout an ADMIN account");
        }

        target.invalidateSession();
        userRepository.Save(target);

        auditLogger.logAction(adminId, "FORCE_LOGOUT", "userId=" + targetUserId.value());
    }
}
