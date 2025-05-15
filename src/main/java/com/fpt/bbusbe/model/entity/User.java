package com.fpt.bbusbe.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.bbusbe.model.enums.Gender;
import com.fpt.bbusbe.model.enums.UserStatus;
import com.fpt.bbusbe.model.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "tbl_user")
public class User extends AbstractEntity implements UserDetails, Serializable {

    @Column(unique = true, nullable = false, length = 255)
    private String username;

    @Column(length = 255)
    private String password;

    /**
     * Phone number validation for Vietnam's mobile carriers.
     *
     * - Starts with an optional '0' (e.g., 0912345678 or 912345678 are both valid)
     * - Follows with one of the valid prefixes:
     *     - 3[2-9]: Numbers starting from 32 to 39
     *     - 5[6|8|9]: Numbers starting with 56, 58, or 59
     *     - 7[0|6-9]: Numbers starting with 70 or from 76 to 79
     *     - 8[0-6|8|9]: Numbers starting from 80 to 86, or 88, 89
     *     - 9[0-4|6-9]: Numbers starting from 90 to 94, or from 96 to 99
     * - Ends with 7 more digits (total 10 digits if starting with '0')
     *
     * Examples of valid phone numbers:
     * - 0912345678
     * - 84912345678
     * - 0321234567
     * - 0851234567
     *
     * Examples of invalid phone numbers:
     * - 0112345678 (Invalid prefix)
     * - 0951234567 (Prefix 95 is not allowed)
     * - 032123456 (Less than required 10 digits)
     */
    @Pattern(regexp = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$",
            message = "Invalid phone number")
    @Column(unique = true, nullable = false, length = 15)
    private String phone;

    @Column(length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 255)
    private Gender gender;

    @Column(length = 255)
    @Temporal(TemporalType.DATE)
    private Date dob;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String avatar;

    @Column(length = 255)
    private String address;

    @Column(name = "device_token", length = 255)
    private String deviceToken;

    @Column(length = 255)
    private String platform;

    @Column(name = "version_app")
    private String versionApp;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 255)
    private UserType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 255)
    private UserStatus status;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<UserHasRole> roles = new HashSet<>();

    @OneToMany(mappedBy = "approvedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Request> approvedRequests;

    @OneToMany(mappedBy = "sendBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Request> sendRequests;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private Assistant assistant;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private Driver driver;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private Parent parent;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // Get role
        List<Role> roleList = roles.stream().map(UserHasRole::getRole).toList();

        // Get role name
        List<String> roleNames = roleList.stream().map(Role::getName).toList();

        // Add role name to authority
        return roleNames.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status);
    }

    // get roles name
    public List<String> getRoleNames() {
        return roles.stream()
                .map(UserHasRole::getRole)
                .map(Role::getName)
                .toList();
    }

    //get role name
    public String getRoleName() {
        return roles.stream()
                .map(UserHasRole::getRole)
                .map(Role::getName)
                .findFirst()
                .orElse(null);
    }

}
