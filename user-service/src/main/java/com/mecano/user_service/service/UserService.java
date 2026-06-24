package com.mecano.user_service.service;

import com.mecano.user_service.dto.UserRequest;
import com.mecano.user_service.entity.User;
import com.mecano.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    @Transactional
    public User create(UserRequest req) {
        if (repo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        return repo.save(User.builder()
                .authUserId(req.getAuthUserId())
                .name(req.getName())
                .email(req.getEmail())
                .isActive(true)
                .build());
    }

    public User getById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));
    }

    public User getByAuthUserId(UUID authUserId) {
        return repo.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    @Transactional
    public User update(UUID id, UserRequest req) {
        User user = getById(id);
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        return repo.save(user);
    }

    @Transactional
    public void deactivate(UUID id) {
        User user = getById(id);
        user.setActive(false);
        repo.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}