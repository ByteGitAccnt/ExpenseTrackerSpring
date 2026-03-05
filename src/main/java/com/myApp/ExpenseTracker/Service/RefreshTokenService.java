package com.myApp.ExpenseTracker.Service;

import com.myApp.ExpenseTracker.Exeception.InvalidRefreshTokenException;
import com.myApp.ExpenseTracker.Model.RefreshToken;
import com.myApp.ExpenseTracker.Model.User;
import com.myApp.ExpenseTracker.Repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService  {
    private final RefreshTokenRepository refreshTokenRepository;
    //private final Duration refreshTokenDuration = Duration.ofDays(7);
    // for testing
    private final Duration refreshTokenDuration = Duration.ofMinutes(2);

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // delete existing token (one per user policy)
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();// flush , i

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString().replace("-", ""));
        refreshToken.setExpiryDate(
                LocalDateTime.now().plus(refreshTokenDuration)
        );
        refreshToken.setCreatedAt(LocalDateTime.now());

        return refreshTokenRepository.save(refreshToken);
    }
    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }
        User user = refreshToken.getUser();

        return createRefreshToken(user);
    }
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}