package com.fpt.bbusbe.utils;

import com.fpt.bbusbe.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class TokenUtils {
    public static UUID getUserLoggedInId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) auth.getPrincipal();
//        UUID loggedInUser = userDetails.getId();
//        String role = userDetails.getRoleNames().iterator().next();

        return userDetails.getId();
    }
}
