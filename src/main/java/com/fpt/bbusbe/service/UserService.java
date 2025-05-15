package com.fpt.bbusbe.service;

import com.fpt.bbusbe.model.dto.request.user.*;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.dto.response.user.UserPageResponse;
import com.fpt.bbusbe.model.dto.response.user.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserPageResponse findAll(String keyword, String roleName, String sort, int page, int size);

    UserResponse findById(UUID id);

    UserResponse findByUsername(String username);

    UserResponse findByEmail(String email);

    User save(UserCreationRequest req);

    List<User> importUsersFromFile(MultipartFile file, String roleName);

    void update(UserUpdateRequest req);

    String updateAvatarUserLoggedIn(UserUpdateAvatarRequest req);

    String updateAvatar(UserUpdateAvatarRequest req);

    void delete(UUID id);

    void changePassword(UserPasswordRequest req);

    void changeStatus(@Valid UserUpdateRequest req);

    UserResponse findByPhone(String phone);

    Object getEntityByUserId(UUID id);

    Object getEntityByUserPhone(String username);

    void uploadAvatar(@Valid UserUploadImageRequest userUploadImageRequest);

    long countTotalUsers();
    long countActiveUsers();
    long countInactiveUsers();
}