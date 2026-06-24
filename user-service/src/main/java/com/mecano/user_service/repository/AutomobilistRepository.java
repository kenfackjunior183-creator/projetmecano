package com.mecano.user_service.repository;

import com.mecano.user_service.entity.Automobilist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutomobilistRepository extends JpaRepository<Automobilist, UUID> {
    Optional<Automobilist> findByEmail(String email);
    Optional<Automobilist> findByAuthUserId(UUID authUserId);
    boolean existsByEmail(String email);
}