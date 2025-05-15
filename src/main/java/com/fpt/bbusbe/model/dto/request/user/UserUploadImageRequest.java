package com.fpt.bbusbe.model.dto.request.user;

import com.fpt.bbusbe.model.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UserUploadImageRequest {
    private Role role;
    private MultipartFile[] avatars;
}
