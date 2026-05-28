package com.novacart.store.service;

import com.novacart.store.dto.UserDtos;
import com.novacart.store.entity.User;
import com.novacart.store.exception.ResourceNotFoundException;
import com.novacart.store.repository.UserRepository;
import com.novacart.store.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public UserDtos.UserSummary currentUser() {
        return UserDtos.UserSummary.from(currentUserService.requireCurrentUser());
    }

    @Transactional(readOnly = true)
    public UserDtos.PublicUser getPublicProfile(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return UserDtos.PublicUser.from(u);
    }

    @Transactional
    public UserDtos.UserSummary updateProfile(UserDtos.UpdateProfileRequest request) {
        User current = currentUserService.requireCurrentUser();
        if (request.displayName() != null) current.setDisplayName(request.displayName().trim());
        if (request.avatarUrl() != null) current.setAvatarUrl(request.avatarUrl());
        if (request.bio() != null) current.setBio(request.bio());
        if (request.location() != null) current.setLocation(request.location());
        userRepository.save(current);
        return UserDtos.UserSummary.from(current);
    }
}
