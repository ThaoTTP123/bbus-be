package com.fpt.bbusbe.model.dto.request.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
public class UserUpdateAvatarRequest {
    private UUID id;
    private MultipartFile[] avatar;
}
