package com.fpt.bbusbe.service.impl;

import com.fpt.bbusbe.exception.ImportException;
import com.fpt.bbusbe.model.dto.request.user.*;
import com.fpt.bbusbe.model.dto.response.excel.UserImportResult;
import com.fpt.bbusbe.model.entity.*;
import com.fpt.bbusbe.model.enums.UserStatus;
import com.fpt.bbusbe.model.enums.UserType;
import com.fpt.bbusbe.model.dto.response.user.UserPageResponse;
import com.fpt.bbusbe.model.dto.response.user.UserResponse;
import com.fpt.bbusbe.exception.InvalidDataException;
import com.fpt.bbusbe.exception.ResourceNotFoundException;
import com.fpt.bbusbe.repository.*;
import com.fpt.bbusbe.service.EmailService;
import com.fpt.bbusbe.service.S3Service;
import com.fpt.bbusbe.service.UserService;
import com.fpt.bbusbe.utils.ExcelHelper;
import com.fpt.bbusbe.utils.PasswordUtils;
import com.fpt.bbusbe.utils.RegistrationEmailTemplateBuilder;
import com.fpt.bbusbe.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fpt.bbusbe.utils.PasswordUtils.generateRandomPassword;

@Service
@Slf4j(topic = "USER-SERVICE")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final AssistantRepository assistantRepository;
    private final DriverRepository driverRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final EmailService emailService;

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long countActiveUsers() {
        return userRepository.countByStatus(UserStatus.ACTIVE);
    }

    @Override
    public long countInactiveUsers() {
        return userRepository.countByStatus(UserStatus.INACTIVE);
    }

    @Override
    public UserPageResponse findAll(String keyword, String roleName, String sort, int page, int size) {
        log.info("findAll start");

        // Sorting
        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        // Xử lý trường hợp FE muốn bắt đầu với page = 1
        int pageNo = (page > 0) ? page - 1 : 0;

        // Paging
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        // Chuẩn hóa `keyword` (nếu có) để tránh lỗi query
        String formattedKeyword = (StringUtils.hasLength(keyword)) ? "%" + keyword.toLowerCase() + "%" : "%";

        // Nếu không có roleName, truyền `null` để bỏ qua lọc theo role
        Page<User> entityPage;
        if (roleName == null) {
             entityPage = userRepository.searchByKeyword(formattedKeyword, pageable);

        } else {
            entityPage = userRepository.searchByKeywordAndRole(formattedKeyword, roleName, pageable);

        }
        return getUserPageResponse(page, size, entityPage);
    }


//    @Override
//    public UserPageResponse findAll(String keyword, String sort, int page, int size) {
//        log.info("findAll start");
//
//        // Sorting
//        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
//        if (StringUtils.hasLength(sort)) {
//            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // tencot:asc|desc
//            Matcher matcher = pattern.matcher(sort);
//            if (matcher.find()) {
//                String columnName = matcher.group(1);
//                if (matcher.group(3).equalsIgnoreCase("asc")) {
//                    order = new Sort.Order(Sort.Direction.ASC, columnName);
//                } else {
//                    order = new Sort.Order(Sort.Direction.DESC, columnName);
//                }
//            }
//        }
//
//        // Xu ly truong hop FE muon bat dau voi page = 1
//        int pageNo = 0;
//        if (page > 0) {
//            pageNo = page - 1;
//        }
//
//        // Paging
//        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));
//
//        Page<User> entityPage;
//
//        if (StringUtils.hasLength(keyword)) {
//            keyword = "%" + keyword.toLowerCase() + "%";
//            entityPage = userRepository.searchByKeyword(keyword, pageable);
//        } else {
//            entityPage = userRepository.findAll(pageable);
//        }
//
//        return getUserPageResponse(page, size, entityPage);
//    }

    @Override
    public UserResponse findById(UUID id) {
        User user = getUserEntity(id);
        //Get presigned url for avatar
        String avatarUrl = null;
        if (user.getAvatar() != null) {
            try {
                avatarUrl = s3Service.generatePresignedUrl(user.getRoleName().toLowerCase() + "s/" + user.getAvatar());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return UserResponse.builder()
                .userId(id)
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .avatar(avatarUrl)
                .dob(user.getDob())
                .gender(user.getGender())
                .phone(user.getPhone())
                .role(user.getRoleName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt() : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt() : null)
                .build();
    }

    @Override
    public UserResponse findByUsername(String username) {
        return null;
    }

    @Override
    public UserResponse findByEmail(String email) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(UserCreationRequest req) {
        User userByEmail = userRepository.findByEmail(req.getEmail());
        if (userByEmail != null) {
            throw new InvalidDataException("User with this email: " + req.getEmail() + " already exists");
        }

        User userByPhone = userRepository.findByPhone(req.getPhone());
        if (userByPhone != null) {
            throw new InvalidDataException("User with this phone: " + req.getPhone() + " already exists");
        }

        MultipartFile avatar = req.getAvatar()[0];

        String pw = generateRandomPassword();

        // insert into tbl_user
        User user = new User();
        user.setName(req.getName());
        user.setGender(req.getGender());
        user.setDob(req.getDob());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(pw);
        user.setPassword(passwordEncoder.encode(pw));
        user.setAddress(req.getAddress());
        user.setAvatar(avatar.getOriginalFilename());
        user.setType(UserType.USER);
        user.setStatus(UserStatus.ACTIVE);
        log.info("Saving user {}", user);

        userRepository.save(user);

        // insert into tbl_user_has_role
        Role role = roleRepository.findByName(req.getRole().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        UserHasRole userHasRole = new UserHasRole(user, role);
        userHasRoleRepository.save(userHasRole);

        Set<UserHasRole> userHasRoles = new HashSet<>();
        userHasRoles.add(userHasRole);
        user.setRoles(userHasRoles);

        // insert into role table
        String roleName = req.getRole().toString();

        switch (roleName) {
            case "SYSADMIN", "ADMIN":
                break;
            case "PARENT":
                Parent parent = new Parent();
                parent.setUser(user);
                parentRepository.save(parent);
                break;
            case "DRIVER":
                Driver driver = new Driver();
                driver.setUser(user);
                driverRepository.save(driver);
                break;
            case "ASSISTANT":
                Assistant assistant = new Assistant();
                assistant.setUser(user);
                assistantRepository.save(assistant);
                break;
            default:
                throw new InvalidDataException("Invalid role: " + roleName);
        }


        String emailContent = RegistrationEmailTemplateBuilder.buildRegistrationEmail(
                user.getName(),
                user.getPhone(),
                pw, // mật khẩu vừa tạo (chưa mã hóa)
                user.getRoleName()
        );

        emailService.sendEmail(
                user.getEmail(),
                "BBUS - Chào mừng bạn gia nhập hệ thống",
                emailContent
        );

        // save avatar to s3
        try {
            s3Service.uploadFile(
                    user.getRoleName().toLowerCase() + "s/" + user.getAvatar(),
                    avatar.getInputStream(),
                    avatar.getSize(),
                    avatar.getContentType()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public User save(UserCreationNoImageRequest req) {
        User userByEmail = userRepository.findByEmail(req.getEmail());
        if (userByEmail != null) {
            throw new InvalidDataException("User with this email: " + req.getEmail() + " already exists");
        }

        User userByPhone = userRepository.findByPhone(req.getPhone());
        if (userByPhone != null) {
            throw new InvalidDataException("User with this phone: " + req.getPhone() + " already exists");
        }

        String pw = generateRandomPassword();

        // insert into tbl_user
        User user = new User();
        user.setName(req.getName());
        user.setGender(req.getGender());
        user.setDob(req.getDob());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setUsername(pw);
        user.setPassword(passwordEncoder.encode(pw));
        user.setAddress(req.getAddress());
        user.setAvatar(req.getAvatar());
        user.setType(UserType.USER);
        user.setStatus(UserStatus.ACTIVE);
        log.info("Saving user {}", user);

        userRepository.save(user);

        // insert into tbl_user_has_role
        Role role = roleRepository.findByName(req.getRole().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        UserHasRole userHasRole = new UserHasRole(user, role);
        userHasRoleRepository.save(userHasRole);

        Set<UserHasRole> userHasRoles = new HashSet<>();
        userHasRoles.add(userHasRole);
        user.setRoles(userHasRoles);

        // insert into role table
        String roleName = req.getRole().toString();

        switch (roleName) {
            case "PARENT":
                Parent parent = new Parent();
                parent.setUser(user);
                parentRepository.save(parent);
                break;
            case "DRIVER":
                Driver driver = new Driver();
                driver.setUser(user);
                driverRepository.save(driver);
                break;
            case "ASSISTANT":
                Assistant assistant = new Assistant();
                assistant.setUser(user);
                assistantRepository.save(assistant);
                break;
            default:
                throw new InvalidDataException("Invalid role: " + roleName);
        }


        String emailContent = RegistrationEmailTemplateBuilder.buildRegistrationEmail(
                user.getName(),
                user.getPhone(),
                pw, // mật khẩu vừa tạo (chưa mã hóa)
                user.getRoleName()
        );

        emailService.sendEmail(
                user.getEmail(),
                "BBUS - Chào mừng bạn gia nhập hệ thống",
                emailContent
        );

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<User> importUsersFromFile(MultipartFile file, String roleName) {
        UserImportResult result = ExcelHelper.excelToUsers(file, roleName);

        // Nếu có lỗi, không insert, mà ném exception chứa lỗi
        if (!result.getErrorRows().isEmpty()) {
            throw new ImportException("Import thất bại với nhiều lỗi trong file", result.getErrorRows());
        }

        // Nếu không có lỗi, thì tiếp tục insert
        return result.getValidUsers().stream().map(this::save).collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserUpdateRequest req) {
        log.info("Updating user {}", req);

        List<User> userByEmailAndPhone = userRepository.findByEmailOrPhoneOrId(req.getEmail(), req.getPhone(), req.getId());
        if (!userByEmailAndPhone.isEmpty()) {
            boolean emailExisted = false;
            boolean phoneExisted = false;
            boolean idExisted = false;
            for (User user : userByEmailAndPhone) {
                if (!user.getId().equals(req.getId())) {
                    if (user.getEmail().equals(req.getEmail())) {
                        emailExisted = true;
                    }
                    if (user.getPhone().equals(req.getPhone())) {
                        phoneExisted = true;
                    }
                }else
                    idExisted = true;
            }
            if(!idExisted) {
                throw new InvalidDataException("Not found user with id: " + req.getId());
            }
            if(emailExisted && phoneExisted) {
                throw new InvalidDataException("Mail " + req.getEmail() + " and Phone " + req.getPhone() +  " already exists");
            }else if(emailExisted) {
                throw new InvalidDataException("Email " + req.getEmail() + " already exists");
            }else if(phoneExisted) {
                throw new InvalidDataException("Phone " + req.getPhone() + " already exists");
            }
        }

        //get user by id
        User user = getUserEntity(req.getId());
        //set data
        user.setName(req.getName() == null ? user.getName() : req.getName());
        user.setGender(req.getGender() == null ? user.getGender() : req.getGender());
        user.setDob(req.getDob() == null ? user.getDob() : req.getDob());
        user.setEmail(req.getEmail() == null ? user.getEmail() : req.getEmail());
        user.setPhone(req.getPhone() == null ? user.getPhone() : req.getPhone());
        user.setUsername(req.getUsername() == null ? user.getUsername() : req.getUsername());
        user.setStatus(req.getStatus() == null ? user.getStatus() : req.getStatus());
        user.setAddress(req.getAddress() == null ? user.getAddress() : req.getAddress());
        userRepository.save(user);

    }

    @Override
    @Transactional
    public void delete(UUID id) {
        //get user by id
        User user = getUserEntity(id);
        userRepository.deleteById(id);

        //delete avatar in s3
        try {
            s3Service.deleteFile(user.getRoleName().toLowerCase() + "s/" + user.getAvatar());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("Deleted user {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserPasswordRequest req) {
        // Get user by id
        User user = getUserEntity(req.getId());

        // Check if the current password matches the password in the request
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            log.warn("Current password does not match for user {}", user);
            throw new InvalidDataException("Current password does not match");
        }

        // Validate the new password
        if (!PasswordUtils.isValidPassword(req.getPassword())) {
            log.warn("New password does not meet the policy requirements for user {}", user);
            throw new InvalidDataException("New password does not meet the policy requirements: At least 8 characters, 1 uppercase letter, 1 number");
        }

        // Check if the new password and confirm password match
        if (req.getPassword().equals(req.getConfirmPassword())) {
            // Encode and set the new password
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            userRepository.save(user);
            log.info("Changing password for user {}", user);
        } else {
            log.warn("Password and confirm password do not match for user {}", user);
            throw new InvalidDataException("Password and confirm password do not match");
        }
    }

    @Override
    public void changeStatus(UserUpdateRequest req) {
        //get user by id
        User user = getUserEntity(req.getId());

        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }
        userRepository.save(user);
        log.info("Changing password for user {}", user);
    }

    /**
     * Get user by id
     *
     * @param id ID of user
     * @return User
     */
    private User getUserEntity(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Get entity by user id, based on role
     *
     * @param id ID of user
     * @return Object
     */
    public Object getEntityByUserId(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String roleName = user.getRoleName();
        Object entity = switch (roleName) {
            case "PARENT" -> parentRepository.findByUser(user);
            case "DRIVER" -> driverRepository.findByUser(user);
            case "ASSISTANT" -> assistantRepository.findByUser(user);
            default -> null;
        };
        log.info("Found entity: {}", entity);
        return entity;

    }

    public Object getEntityByUserPhone(String phone) {
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        String roleName = user.getRoleName();
        Object entity = switch (roleName) {
            case "PARENT" -> parentRepository.findByUser(user);
            case "DRIVER" -> driverRepository.findByUser(user);
            case "ASSISTANT" -> assistantRepository.findByUser(user);
            default -> null;
        };
        log.info("Found entity: {}", entity);
        return entity;
    }

    private UserPageResponse getUserPageResponse(int page, int size, Page<User> userEntities) {
        log.info("Convert User Entity Page");

        List<UserResponse> userList = userEntities.stream().map(entity -> {
                    //Get presigned url for avatar
                    String avatarUrl = null;
                    if (entity.getAvatar() != null) {
                        try {
                            avatarUrl = s3Service.generatePresignedUrl(entity.getRoleName().toLowerCase() + "s/" + entity.getAvatar());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }
                    return UserResponse.builder()
                            .userId(entity.getId())
                            .name(entity.getName())
                            .gender(entity.getGender())
                            .dob(entity.getDob())
                            .username(entity.getUsername())
                            .phone(entity.getPhone())
                            .email(entity.getEmail())
                            .address(entity.getAddress())
                            .avatar(avatarUrl)
                            .status(entity.getStatus())
                            .role(entity.getRoleName())
                            .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : null)
                            .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : null)
                            .build();
                }
        ).toList();

        UserPageResponse response = new UserPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(userEntities.getTotalElements());
        response.setTotalPages(userEntities.getTotalPages());
        response.setUsers(userList);

        return response;
    }

    @Override
    public UserResponse findByPhone(String phone) {
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return UserResponse.builder()
                .userId(user.getId())
                .role(user.getRoleName())
                .status(user.getStatus())
                .build();
    }

    @Override
    public String updateAvatarUserLoggedIn(UserUpdateAvatarRequest req) {
        UUID userId = TokenUtils.getUserLoggedInId();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update avatar image to alibaba cloud oss
        MultipartFile file = req.getAvatar()[0];
        try {
            s3Service.uploadFile(
                    user.getRoleName().toLowerCase() + "s/" + file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        user.setAvatar(file.getOriginalFilename());
        userRepository.save(user);
        try {
            return s3Service.generatePresignedUrl(user.getRoleName().toLowerCase() + "s/" + user.getAvatar());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public String updateAvatar(UserUpdateAvatarRequest req) {
        User user = userRepository.findById(req.getId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update avatar image to alibaba cloud oss
        MultipartFile file = req.getAvatar()[0];
        try {
            s3Service.uploadFile(
                    user.getRoleName().toLowerCase() + "s/" + file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        user.setAvatar(file.getOriginalFilename());
        userRepository.save(user);
        try {
            return s3Service.generatePresignedUrl(user.getRoleName().toLowerCase() + "s/" + user.getAvatar());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void uploadAvatar(UserUploadImageRequest userUploadImageRequest) {
        com.fpt.bbusbe.model.enums.Role role = userUploadImageRequest.getRole();
        MultipartFile[] files = userUploadImageRequest.getAvatars();
        for (MultipartFile file : files) {
            try {
                s3Service.uploadFile(
                        role.toString().toLowerCase() + "s/" + file.getOriginalFilename(),
                        file.getInputStream(),
                        file.getSize(),
                        file.getContentType()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}