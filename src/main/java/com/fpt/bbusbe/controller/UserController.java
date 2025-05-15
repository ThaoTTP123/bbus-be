package com.fpt.bbusbe.controller;


import com.fpt.bbusbe.exception.ForBiddenException;
import com.fpt.bbusbe.model.dto.request.user.*;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.model.dto.response.user.UserResponse;
import com.fpt.bbusbe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller")
@Slf4j(topic = "USER-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user list", description = "API retrieve users from db")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> getList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String roleName,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "100000") int size) {
        log.info("Get user list");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "user list");
        result.put("data", userService.findAll(keyword, roleName, sort, page, size));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get user detail", description = "API retrieve user detail by ID")
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getUserDetail(@PathVariable("userId") UUID id
    ) {
        log.info("Get user detail by ID: {}", id);

        // Get logged in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) auth.getPrincipal();
        UUID loggedInUser = userDetails.getId();
        String role = userDetails.getRoleNames().iterator().next();
//        log.info("role: {}", role);

        if (role.equals("ADMIN") || role.equals("SYSADMIN")) {
            log.info("role: {}", role);
        } else {
            //Check id
            if (!loggedInUser.equals(id)) {
                throw new ForBiddenException("You are not allowed to access this resource");
            }
        }

        UserResponse u = userService.findById(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get user detail");
        result.put("data", u);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Create user", description = "API add new user to db")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> createUser(@ModelAttribute @Valid UserCreationRequest userCreationRequest
    ) {
        log.info("Create user: {}", userCreationRequest);

        User user = userService.save(userCreationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "user created successfully");
        result.put("data", user.getId());

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Upload images of users", description = "API upload images of users based on role")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> uploadAvatar(@ModelAttribute @Valid UserUploadImageRequest userUploadImageRequest
    ) {

        userService.uploadAvatar(userUploadImageRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "upload images successfully");

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Import user by excel", description = "API import users to db")
    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> importUsers(@RequestParam("file") MultipartFile file,
                                              @RequestParam(required = true) String roleName
    ) {
        log.info("Import users from file");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "user created successfully");
        result.put("data", userService.importUsersFromFile(file, roleName));

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update user", description = "API update user in db")
    @PutMapping("/upd")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest
    ) {
        log.info("Updating user: {}", userUpdateRequest);

        userService.update(userUpdateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "user updated successfully");
        result.put("data", "");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }


    @Operation(summary = "Update user avatar", description = "API update user in db")
    @PatchMapping(value = "/upd-avatar-user-logged-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> updateUserAvatarLoggedIn(@ModelAttribute @Valid UserUpdateAvatarRequest userUpdateRequest
    ) {
        log.info("Updating user: {}", userUpdateRequest);

        String avatarUrl = userService.updateAvatarUserLoggedIn(userUpdateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "user updated successfully");
        result.put("data", avatarUrl);

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Update user avatar", description = "API update user in db")
    @PatchMapping(value = "/upd-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> updateUserAvatar(@ModelAttribute @Valid UserUpdateAvatarRequest userUpdateRequest
    ) {
        log.info("Updating user: {}", userUpdateRequest);

        userService.updateAvatar(userUpdateRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "user updated successfully");
        result.put("data", "");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Change password", description = "API change password for user")
    @PatchMapping("/change-pwd")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> changePassword(@RequestBody @Valid UserPasswordRequest userPasswordRequest
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "user change password successfully");
        result.put("data", "");
        userService.changePassword(userPasswordRequest);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Update status", description = "API change status account for user")
    @PatchMapping("/change-status")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> changeStatus(@RequestBody @Valid UserUpdateRequest userUpdateRequest
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "user change status successfully");
        result.put("data", "");
        userService.changeStatus(userUpdateRequest);
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @GetMapping("/confirm-email")
    @PreAuthorize("isAuthenticated()")
    public void confirmEmail(@RequestParam String secretCode, HttpServletResponse response) throws IOException {
        log.info("Confirm email: {}", secretCode);

        try {
            // TODO check or compare secretCode from database
        } catch (Exception e) {
            log.error("Confirm email was failure!, errorMessage = {}", e.getMessage());
        } finally {
            response.sendRedirect("https://google.com");
        }
    }

    @Operation(summary = "Delete user", description = "API inactivate user")
    @DeleteMapping("/del/{userId}")
    @PreAuthorize("hasAnyAuthority('SYSADMIN', 'ADMIN')")
    public ResponseEntity<Object> deleteUser(@PathVariable("userId") UUID id
    ) {
        log.info("Deleting user: {}", id);

        userService.delete(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "user deleted successfully");
        result.put("data", "");

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Get entity by user ID", description = "API to retrieve entity by user ID based on role")
    @GetMapping("/entity/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getEntityByUserId(@PathVariable("userId") UUID id) {
        log.info("Get entity by user ID: {}", id);

        Object entity = userService.getEntityByUserId(id);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "get entity by user ID");
        result.put("data", entity);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}