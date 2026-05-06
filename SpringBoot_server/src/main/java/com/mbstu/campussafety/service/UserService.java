package com.mbstu.campussafety.service;

import com.mbstu.campussafety.dto.user.UserDTO;
import com.mbstu.campussafety.entity.Role;
import com.mbstu.campussafety.entity.User;
import com.mbstu.campussafety.exception.ResourceNotFoundException;
import com.mbstu.campussafety.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapUserToDTO(user);
    }


    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapUserToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        log.debug("Fetching all users");
        List<UserDTO> list = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserDTO userDTO = mapUserToDTO(user);
            list.add(userDTO);
        }
        return list;
    }

    public List<UserDTO> getUsersByRole(String roleName) {
        log.debug("Fetching users with role: {}", roleName);
        return userRepository.findByRolesName(roleName).stream()
            .map(this::mapUserToDTO)
            .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        return mapUserToDTO(updatedUser);
    }

    public void updateUserLocation(Long userId, Double latitude, Double longitude) {
        log.debug("Updating location for user: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        userRepository.save(user);
    }

    public void updateFcmToken(Long userId, String fcmToken) {
        log.debug("Updating FCM token for user: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .isVerified(user.getIsVerified())
            .latitude(user.getLatitude())
            .longitude(user.getLongitude())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
