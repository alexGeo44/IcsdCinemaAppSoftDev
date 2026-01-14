package com.cinema.application.users;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.ValidationException;
import com.cinema.domain.entity.User;
import com.cinema.domain.port.UserRepository;
import com.cinema.infrastructure.security.TokenService;
import com.cinema.infrastructure.security.TokenValidator;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ValidateTokenUseCase {

    private final TokenValidator tokenValidator;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public ValidateTokenUseCase(TokenValidator tokenValidator,
                                TokenService tokenService,
                                UserRepository userRepository) {
        this.tokenValidator = Objects.requireNonNull(tokenValidator);
        this.tokenService = Objects.requireNonNull(tokenService);
        this.userRepository = Objects.requireNonNull(userRepository);
    }


    public TokenData validate(String token) {
        if (token == null || token.isBlank()) {
            throw new ValidationException("", "Token cannot be null or empty");
        }


        if (tokenService.isInvalidated(token)) {
            throw new AuthorizationException("TOKEN_REVOKED: Token invalidated");
        }


        TokenValidator.TokenData raw = tokenValidator.validate(token);

        User user = userRepository.findById(raw.userId())
                .orElseThrow(() -> new NotFoundException("User", "User not found"));

        if (!user.isActive()) {
            throw new AuthorizationException("ACCOUNT_INACTIVE: Account inactive");
        }


        String currentJti = user.currentJti();
        if (currentJti == null || !currentJti.equals(raw.jti())) {
            throw new AuthorizationException("TOKEN_REVOKED: Token is not current");
        }


        return new TokenData(
                user.id().value(),
                user.baseRole().name(),
                raw.jti()
        );
    }

    public record TokenData(Long userId, String role, String jti) {}
}
