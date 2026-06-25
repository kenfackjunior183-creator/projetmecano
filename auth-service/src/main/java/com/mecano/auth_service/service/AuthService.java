package com.mecano.auth_service.service;

import com.mecano.auth_service.dto.*;
import com.mecano.auth_service.entity.RefreshToken;
import com.mecano.auth_service.entity.Role;
import com.mecano.auth_service.entity.UserCredential;
import com.mecano.auth_service.repository.RefreshTokenRepository;
import com.mecano.auth_service.repository.UserCredentialRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserCredentialRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Super Admin configuration ────────────────────────────────
    @Value("${app.admin.email:admin@mecano.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.name:Super Admin}")
    private String adminName;

    /**
     * Creates the super admin user at application startup if it doesn't exist.
     * This ensures there is always an ADMIN user to manage the platform.
     */
    @PostConstruct
    @Transactional
    public void createSuperAdminIfNotExists() {
        if (!userRepo.existsByEmail(adminEmail)) {
            UserCredential admin = UserCredential.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();

            userRepo.save(admin);
            log.info("✅ Super admin created successfully – email: {}, role: ADMIN", adminEmail);
        } else {
            log.debug("ℹ️ Super admin already exists – email: {}", adminEmail);
        }
    }

    // ── Inscription ─────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé : " + request.getEmail());
        }

        UserCredential user = UserCredential.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();

        userRepo.save(user);
        return buildAuthResponse(user);
    }

    // ── Connexion ───────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        UserCredential user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Révoquer les anciens refresh tokens
        refreshTokenRepo.revokeAllUserTokens(user.getId());

        return buildAuthResponse(user);
    }

    // ── Mise à jour du rôle ────────────────────────────────────
    @Transactional
    public AuthResponse upgradeRole(String userId, Role newRole) {
        UserCredential user = userRepo.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Seulement les upgrades autorisés
        if (user.getRole() == Role.USER && newRole == Role.AUTOMOBILIST) {
            user.setRole(Role.AUTOMOBILIST);
        } else if (user.getRole() == Role.USER && newRole == Role.MECHANIC) {
            user.setRole(Role.MECHANIC);
        } else if (user.getRole() == Role.USER && newRole == Role.ADMIN) {
            user.setRole(Role.ADMIN);
        } else {
            throw new RuntimeException("Changement de rôle non autorisé");
        }

        userRepo.save(user);
        return buildAuthResponse(user);
    }

    // ── Refresh Token ───────────────────────────────────────────
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepo.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token introuvable"));

        if (stored.isRevoked()) {
            throw new RuntimeException("Refresh token révoqué");
        }
        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expiré");
        }

        UserCredential user = stored.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .userId(user.getId().toString())
                .accessToken(newAccessToken)
                .refreshToken(stored.getToken())
                .tokenType("Bearer")
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // ── Helper ──────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(UserCredential user) {
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Persister le refresh token
        refreshTokenRepo.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build());

        return AuthResponse.builder()
                .userId(user.getId().toString())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(user.getRole().name())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}